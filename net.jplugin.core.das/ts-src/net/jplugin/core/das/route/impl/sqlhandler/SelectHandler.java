package net.jplugin.core.das.route.impl.sqlhandler;

import net.jplugin.common.kits.StringKit;
import net.jplugin.core.das.route.api.TablesplitException;
import net.jplugin.core.das.route.impl.parser.SqlWordsWalker;

public class SelectHandler extends AbstractCommandHandler{

	@Override
	String walkTableName(SqlWordsWalker walker) {
		if (!walker.nextUntilIgnoreCase("FROM")) 
			throw new TablesplitException("Can't find FROM word in insert sql."+walker.sql);
		if (!walker.next())
			throw new TablesplitException("Can't find TableName in insert sql."+walker.sql);
		if (StringKit.isNull(walker.word)) 
			throw new TablesplitException("Found null TableName in insert sql."+walker.sql);
		if ("(".equals(walker.word)){
			//如果碰到(，找下一个，但不会循环找
			walker.nextUntilIgnoreCase("FROM");
			if (!walker.next())
				throw new TablesplitException("Can't find TableName in insert sql."+walker.sql);
			if (StringKit.isNull(walker.word)) 
				throw new TablesplitException("Found null TableName in insert sql."+walker.sql);
		}
		String tableName = walker.word;
		return tableName;
	}

	@Override
	KeyResult walkToGetKeyColumnInfo(SqlWordsWalker w, String tableName,String keyField) {
		
		//TABLE名称后面如果是字符串那就是别名，否则无别名
		String tableAliasName=null;
		if (!w.next())
			throw new TablesplitException("the sql is not complete. sql:"+w.sql);
		if (",".equals(w.word)){
			tableAliasName=tableName;
			if (!w.nextUntilIgnoreCase("WHERE")){
				throw new TablesplitException("Can't find where form sql. sql:"+w.sql);
			}
		}else if ("WHERE".equalsIgnoreCase(w.word)){
			tableAliasName=tableName;
		}else{
			tableAliasName=w.word;
			if (!w.nextUntilIgnoreCase("WHERE")){
				throw new TablesplitException("Can't find where form sql. sql:"+w.sql);
			}
		}
		
		//now position is at 'WHERE' and tableAliasName is ok
		
		String colFullName= tableAliasName +"."+keyField;
		
		boolean success=false;
		int paramIndex = 0;
		while(w.next()){
			if (keyField.equalsIgnoreCase(w.word) || colFullName.equalsIgnoreCase(w.word)){
				success = true;
				break;
			}else if ("?".equalsIgnoreCase(w.word)){
				paramIndex++;
			}
		}
		
		if (!success){
			throw new TablesplitException("Can't match the key field. sql:"+w.sql);
		}
		
		KeyResult result = new KeyResult();
		if (w.next() && "=".equals( w.word )){
			if (!w.next())throw new TablesplitException("Thesql is not complete. sql:"+w.sql);
			if ("?".equals(w.word)){
				result.isParamedKey = true;
				result.keyParamIndex = paramIndex;
			}else{
				result.isParamedKey = false;
				result.keyConstValue = getConstKeyValueFromSql(w.word);
			}
		}else{
			throw new TablesplitException("The word after key field must be '='. sql:"+w.sql);
		}

		return result;
	}

	@Override
	String getFinalSql(SqlWordsWalker walker, String sourceSql, String finalTableName) {
		String finalSql = StringKit.repaceFirst(walker.sql,sourceSql,finalTableName);
		//处理带字段的表名
		finalSql = StringKit.replaceStr(finalSql," "+sourceSql+"."," "+finalTableName+".");
		return finalSql;
	}
}
