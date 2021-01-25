package com.demo.tcp.main;

import org.apache.log4j.Logger;

import com.demo.tcp.ice.impl.SwitchI;

import switcher.ISwitchCallbackPrx;
import switcher.SwitchException;

public class SwitchUtil {
	private static Logger LOGGER = Logger.getLogger(SwitchUtil.class);

	/**
	 * 发送数据
	 * 
	 * @author jerome
	 * @param sn
	 *            和服务端心跳的标识
	 * @param msg
	 *            字符串
	 * @return
	 */
	public static boolean sendMsg(String sn, String msg) {

		LOGGER.info("ice tcp send params sn = " + sn);

		boolean sendResult = true;

		if (!SwitchI.switchCallbackPrxCacheMap.containsKey(sn)) {
			LOGGER.warn("PrxCacheMap is no exist, ice tcp send end, sendResult = false\n");
			return false;
		}

		ISwitchCallbackPrx switchCallbackPrx = SwitchI.switchCallbackPrxCacheMap.get(sn).getiSwitchCallbackPrx();
		if (null != switchCallbackPrx) {
			try {
				sendResult = switchCallbackPrx.sendMsg(msg);
			} catch (SwitchException e) {
				LOGGER.error(e.toString());
				e.printStackTrace();
				sendResult = false;
			}
		} else {
			sendResult = false;
		}

		LOGGER.info("ice tcp send end, sendResult = " + sendResult + "\n");
		LOGGER.info("sendResult = " + sendResult);
		return sendResult;
	}
}
