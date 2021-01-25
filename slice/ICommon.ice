module switcher
{
	/**
	 * <b><font color="#ff0000">SMHome所有接口类的共同父类</font></b>
	 */
	interface IBase
	{

	};
	
	/**
	 * 	 Switch异常,
	 * 	 通过code区分不同的异常。
	 */
	exception SwitchException
	{
		/**
		 * 异常代码
		 */
		int code;
		/**
		 * 异常信息
		 */
		string message;
	};
	
	
	/**
	 *  基类:
	 */
	class BaseInfo
	{
			
	};

	/**
	 * 基类集合
	 */
	["java:type:java.util.ArrayList<switcher.BaseInfo>:java.util.ArrayList<switcher.BaseInfo>"]
	sequence < BaseInfo > BaseInfoSeq;
};
