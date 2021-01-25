package com.demo.tcp.main;

import java.io.File;

import org.apache.log4j.Logger;

import com.demo.tcp.ice.impl.SwitchI;

/**
 * 启动服务端
 * 
 * @author jerome
 */
public class Main extends Ice.Application {

	private static String APP_MAIN = System.getenv("APP_HOME");
	private static String SIP_CONFIG = "../src/main/resources/syscfg.properties";
	private static Logger LOGGER = Logger.getLogger(Main.class);

	public static void main(String[] args) throws Exception {

		// 服务端发送数据给客户端 SwitchClient.java要运行
		new Thread(new Runnable() {
			@Override
			public void run() {
				LOGGER.info("send thread start.");
				try {
					Thread.sleep(30000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				// 和服务端心跳的标识
				String sn = "0481deb6494848488048578316516694";
				String msg = "test msg.";
				LOGGER.info("result = " + SwitchUtil.sendMsg(sn, msg));
			}
		}).start();

		// 启动ice
		LOGGER.info("===========starting ice service============");
		String confPath = SIP_CONFIG;
		if (null != APP_MAIN) {
			confPath = APP_MAIN + "/src/main/resources/syscfg.properties";
		} else {
			confPath = SIP_CONFIG;
			File file = new File(confPath);
			if (!file.exists()) {
				confPath = System.getProperty("user.dir") + "/src/main/resources/syscfg.properties";
			}
		}
		LOGGER.info("confPath=" + confPath);

		Main app = new Main();

		int status = app.main("Main", args, confPath);
		System.exit(status);
	}

	/*
	 * server run
	 * 
	 * @see Ice.Application#run(java.lang.String[])
	 */
	public int run(String[] args) {
		if (args.length > 1) {
			LOGGER.info(appName() + ": too many arguments");
			LOGGER.info(appName() + ":");
			return 1;
		}

		Ice.ObjectAdapter adapter = communicator().createObjectAdapter("Switch");
		Ice.Properties p = communicator().getProperties();

		adapter.add(new SwitchI(p), communicator().stringToIdentity("Switch"));
		adapter.activate();
		LOGGER.info("ice is started!");
		communicator().waitForShutdown();
		return 0;
	}
}