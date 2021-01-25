package com.demo.tcp;

import java.io.File;

import org.apache.log4j.Logger;

import com.demo.tcp.ice.impl.SwitchCallbackI;
import com.demo.tcp.main.Main;

import Ice.Connection;
import Ice.LocalException;
import Ice.TCPConnectionInfo;
import Ice.UserException;
import switcher.Callback_ISwitch_heartbeat;
import switcher.ISwitchPrx;
import switcher.ISwitchPrxHelper;

public class SwitchClient extends Ice.Application {

	private static Logger LOGGER = Logger.getLogger(Main.class);
	Ice.Communicator communicator = null;
	ISwitchPrx switchPushPrx = null;

	public String sn = "0481deb6494848488048578316516694";
	private String prxStr = "Switch:tcp -h 127.0.0.1 -p 5010";

	static String APP_MAIN = System.getenv("APP_HOME");
	static String PUSH_CLIENT_CONFIG = "/src/test/resources/SwitchClient.conf";

	public SwitchClient() {
	}

	public static void main(String[] args) {

		String confPath = PUSH_CLIENT_CONFIG;
		if (null != APP_MAIN)
			confPath = APP_MAIN + "/src/main/resources/syscfg.properties";
		else {
			confPath = PUSH_CLIENT_CONFIG;
			File file = new File(confPath);
			if (!file.exists()) {
				confPath = System.getProperty("user.dir") + "/src/test/resources/SwitchClient.conf";
			}
		}

		SwitchClient lpClient = new SwitchClient();

		int status = lpClient.main("SwitchClient", args, confPath);

		System.exit(status);
	}

	public int run(String[] args) {
		try {
			communicator = communicator();
			switchPushPrx = ISwitchPrxHelper.checkedCast(communicator.stringToProxy(prxStr));
			switchPushPrx.ice_ping();
		} catch (Ice.LocalException ex) {
			ex.printStackTrace();
		}

		Ice.ObjectAdapter adapter = communicator.createObjectAdapter("");

		Ice.Identity id = new Ice.Identity();
		id.category = "";
		id.name = "SwitchClient";

		adapter.add(new SwitchCallbackI(), id);

		adapter.activate();

		switchPushPrx.ice_getConnection().setAdapter(adapter);

		LOGGER.info("SwitchClient ice is started! " + "getEndpoint = "
				+ switchPushPrx.ice_getConnection().getEndpoint()._toString());

		try {
			String ip = ""; // 缓存当前ip
			int port = 0; // 缓存当前端口
			
			while (true) {

				// ip或端口不一样时，重置适配器（服务端重启的情况）
				if (switchPushPrx == null) {
					break;
				}
				
				Connection connection = null;
				String localIp = null;
				int localPort = 0;
				try {
					connection = switchPushPrx.ice_getConnection();
					TCPConnectionInfo connectionInfo = (TCPConnectionInfo) connection.getInfo();
					localIp = connectionInfo.localAddress;
					localPort = connectionInfo.localPort;
					LOGGER.info("localIp:" + localIp + "; localPort:" + localPort);
					if (!ip.equals(localIp) || port != localPort) {
						ip = localIp;
						port = localPort;
						LOGGER.info("ip or port is change. set adapter.");
						connection.setAdapter(adapter);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				LOGGER.info("SwitchClient is begin heartbeat.");
				
				// 使用异步的方式
				switchPushPrx.begin_heartbeat(id, sn, 1, 2, new Callback_ISwitch_heartbeat() {

					@Override
					public void exception(LocalException __ex) {
					}

					@Override
					public void response(boolean arg) {
						LOGGER.info("heartbeat result = " + arg);
						if (arg) {
							LOGGER.info("心跳成功");
						} else {
							LOGGER.info("心跳失败");
						}
					}

					@Override
					public void exception(UserException ex) {
					}
				});

				LOGGER.info("SwitchClient is end heartbeat.\n");
				Thread.sleep(5000);
			}
			// 可以使用以上的while(true)
			// Thread.sleep(HEARTBEAT_TIME);的方式定时请求保持tcp连接的心跳，这个方式是为了每次心跳都传参
			// 也可以使用一下的方式，使用ice自带的功能保持tcp的连接心跳，无法每次心跳传参
			// holdHeartbeat(switchPushPrx.ice_getConnection());
		} catch (Exception e) {
			e.printStackTrace();
		}

		communicator().waitForShutdown();

		return 0;
	}

	public void destroy() {
		if (null != communicator) {
			communicator.destroy();
		}
	}

	/**
	 * ice自带保持心跳
	 * 
	 * @author jerome
	 * @param con
	 */
	@SuppressWarnings("unused")
	private void holdHeartbeat(Ice.Connection con) {

		con.setCallback(new Ice.ConnectionCallback() {
			@Override
			public void heartbeat(Ice.Connection c) {
				System.out.println("sn:" + sn + " client heartbeat....");
			}

			@Override
			public void closed(Ice.Connection c) {
				System.out.println("sn:" + sn + " " + "closed....");
			}
		});

		// 每30/2 s向对方做心跳
		// 客户端向服务端做心跳 服务端打印服务端的con.setCallback(new Ice.ConnectionCallback()
		con.setACM(new Ice.IntOptional(10), new Ice.Optional<Ice.ACMClose>(Ice.ACMClose.CloseOff),
				new Ice.Optional<Ice.ACMHeartbeat>(Ice.ACMHeartbeat.HeartbeatAlways));

	}

}
