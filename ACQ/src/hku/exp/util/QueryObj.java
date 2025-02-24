package hku.exp.util;
/**
 * @author cslxsun
 * @date Aug 11, 2022
 * query object define
 */
public class QueryObj {
    private int qid;
	private int qCore;
	private int[] qCmty;
	
    public QueryObj(int qid, int qCore, int[] qCmty) {
		this.qid = qid;
		this.qCore = qCore;
		this.qCmty = qCmty;
	}
	public int getqid()
	{
		return qid;
	}
	public int getqCore()
	{
		return qCore;
	}
	public int[] getqCmty()
	{
		return qCmty;
	}
}