package com.demo.tcp.ice.impl;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.demo.tcp.model.SwitchCallbackPrxCache;

import Ice.Current;
import Ice.Identity;
import switcher.ISwitchCallbackPrx;
import switcher.ISwitchCallbackPrxHelper;
import switcher._ISwitchDisp;

/**
 * 服务端接口定义(客户端调用服务端的接口)
 * 
 * @author jerome
 */
public class SwitchI extends _ISwitchDisp {

	private static Logger LOGGER = Logger.getLogger(SwitchI.class);
	private static final long serialVersionUID = 1L;

	// 客户端连接缓存 XXX 超时清除? Redis?
	public static ConcurrentHashMap<String, SwitchCallbackPrxCache> switchCallbackPrxCacheMap = new ConcurrentHashMap<>();
	private SwitchCallbackPrxCache switchCallbackPrxCache = null;

	public SwitchI(Ice.Properties p) {
	}

	/**
	 * 心跳(如果用户自己定时做心跳可以心跳时传参)
	 */
	@Override
	public boolean heartbeat(Identity id, String sn, int netMode, int netStrength, Current current) {
		LOGGER.info(switchCallbackPrxCacheMap.size());
		LOGGER.info("tcp heartbeat begin params sn = " + sn + " id.name = " + id.name + ", category = " + id.category
				+ ", netMode = " + netMode + ", netStrength = " + netStrength);
		Ice.Connection con = current.con;
		Ice.IPConnectionInfo ipConn = (Ice.IPConnectionInfo) con.getInfo();
		if (null != ipConn) {
			LOGGER.info("ipConn remote:" + ipConn.remoteAddress + ":" + ipConn.remotePort);
			LOGGER.info("ipConn local:" + ipConn.localAddress + ":" + ipConn.localPort);
		}

		LOGGER.info("heartbeat");
		// 心跳业务处理

		// 如果已经存在不更新缓存
		if (switchCallbackPrxCacheMap.containsKey(sn)) {
			SwitchCallbackPrxCache switchCallbackPrxCache = switchCallbackPrxCacheMap.get(sn);
			if (ipConn.remoteAddress.equals(switchCallbackPrxCache.getIp())
					&& switchCallbackPrxCache.getPort() == ipConn.remotePort) {
				LOGGER.info("already exist cache, return true\n");
				return true;
			} else {
				switchCallbackPrxCacheMap.remove(sn);
			}
		}

		ISwitchCallbackPrx switchCallbackPrx = ISwitchCallbackPrxHelper.checkedCast(con.createProxy(id));

		switchCallbackPrxCache = new SwitchCallbackPrxCache();
		switchCallbackPrxCache.setiSwitchCallbackPrx(switchCallbackPrx);
		switchCallbackPrxCache.setIp(ipConn.remoteAddress);
		switchCallbackPrxCache.setPort(ipConn.remotePort);

		switchCallbackPrxCacheMap.put(sn, switchCallbackPrxCache);
		// 如果用户不是定时心跳，而是使用ice自带的心跳必须执行以下代码
		holdHeartbeat(current.con);
		LOGGER.info("register end, return true. \n");

		return true;
	}

	/**
	 * ice自带保持心跳
	 * 
	 * @author jerome
	 * @param con
	 */
	private void holdHeartbeat(Ice.Connection con) {
		con.setCallback(new Ice.ConnectionCallback() {
			@Override
			public void heartbeat(Ice.Connection c) {
				LOGGER.debug("service heartbeat...");
			}

			@Override
			public void closed(Ice.Connection c) {
				LOGGER.debug("service close!");
			}
		});

		// 每10/2 s向对方做心跳
		// 服务端向客户端做心跳 客户端打印客户端的con.setCallback(new Ice.ConnectionCallback()
//		con.setACM(new Ice.IntOptional(10), new Ice.Optional<Ice.ACMClose>(Ice.ACMClose.CloseOff),
//				new Ice.Optional<Ice.ACMHeartbeat>(Ice.ACMHeartbeat.HeartbeatAlways));
	}
	
	/**
	 * 客户端可以回调这个接口
	 */
	@Override
	public boolean callBack(String msg, Current current) {

		LOGGER.info("ice tcp callBack start. params msg = " + msg);

		boolean result = true;

		Ice.Connection con = current.con;
		Ice.IPConnectionInfo ipConn = (Ice.IPConnectionInfo) con.getInfo();
		if (null != ipConn) {
			LOGGER.info("ipConn remote:" + ipConn.remoteAddress + ":" + ipConn.remotePort);
			LOGGER.info("ipConn local:" + ipConn.localAddress + ":" + ipConn.localPort);
		}

		LOGGER.info("ice tcp callBack end. return result = " + result);

		return result;
	}

}
