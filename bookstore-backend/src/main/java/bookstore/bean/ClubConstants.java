package bookstore.bean;


public class ClubConstants {

	//讀書會狀態
	public  static final int STATUS_PENDING = 0;	  //審核中
	public static final int STATUS_APPROVED = 1;  //報名中
	public static final int STATUS_REJECTED = 2;  //已駁回
	public static final int STATUS_FULL = 3; 	  //已額滿
	public static final int STATUS_DEADLINE = 4;  //已截止
	public static final int STATUS_ENDED = 5;	  //已結束
	public static final int STATUS_CANCELLED = 6; //已取消
	public static final int STATUS_DRAFT = 7;	  //草稿
	public static final int STATUS_ACCEPT = 8;    //已核准
	
	//使用者角色
	public static final int ROLE_SUPER_ADMIN = 0; //超級管理員
	public static final int ROLE_ADMIN = 1; 	  //一般管理員
	public static final int ROLE_MEMBER = 2;      //一般會員

}


