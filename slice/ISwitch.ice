#include <Ice/BuiltinSequences.ice>
#include <Ice/Identity.ice>
#include "ICommon.ice"
module switcher
{
	/**
	 * 回调客户端接口定义(服务端调用客户端的接口)
	 */
	interface ISwitchCallback
	{	 
		/**
		 * 发送二进制数组
		 * @param byteSeq 二进制数组
		 * @return true/false
		 */
		bool send(Ice::ByteSeq byteSeq) throws SwitchException;
		
		/**
		 * 发送字符串
		 * @param msg 字符串
		 * @return true/false
		 */
		bool sendMsg(string msg) throws SwitchException;
	};
	
	/**
	 * 服务端接口定义(客户端调用服务端的接口)
	 */
	interface ISwitch
	{
		/**
		 * 对服务端进行心跳（无异常则表示成功）
		 * @param sn 设备串号
		 * @param netMode 网络接入方式 0:没有 1:3G 2:4G 3:以太网 4：wifi 5：2G
		 * @param netStrength 网络信号强度
		 */
		bool heartbeat(Ice::Identity id, string sn, int netMode, int netStrength) throws SwitchException;
		
		/**
		 * 设备回调
		 * @param byteSeq 二进制数组
		 * @return true/false
		 */
		bool callBack(string msg) throws SwitchException;
	};
};