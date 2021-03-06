/*
 * Copyright 2008-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package egov.data.ibatis.repository.query;

import java.util.*;

import org.springframework.data.repository.query.*;
import org.springframework.orm.ibatis.*;

import com.ibatis.sqlmap.engine.impl.*;
import com.ibatis.sqlmap.engine.mapping.statement.*;

/**
 * iBatis statement를 호출하는 {@link RepositoryQuery} 구현체
 * 
 * @author Yongkwon Park
 * @author Yunseok Choi
 *
 */
public class SqlMapQuery implements RepositoryQuery {
	
	private final QueryMethod queryMethod;
	private final SqlMapClientTemplate template;
	private final StatementType statementType;

	public SqlMapQuery(QueryMethod queryMethod, SqlMapExecutorDelegate delegate, SqlMapClientTemplate sqlMapClientTemplate) {
		this.queryMethod = queryMethod;
		this.template = sqlMapClientTemplate;
		
		// iBatis에게 statement(query)의 유형(select, insert, update, delete) 얻기
		MappedStatement statement = delegate.getMappedStatement(queryMethod.getNamedQueryName());
		this.statementType = statement.getStatementType();
	}
	
	@Override
	public Object execute(Object[] parameters) {
		
		Object parameterObject = parametersParsing(parameters);
		
		if (StatementType.SELECT.equals(statementType)) {
			if (!queryMethod.isPageQuery() && !queryMethod.isCollectionQuery()) {
	            return template.queryForObject(queryMethod.getNamedQueryName(), parameterObject);
	        } else if (queryMethod.isCollectionQuery()) {
	            return template.queryForList(queryMethod.getNamedQueryName(), parameterObject);
	        }
		} else if (StatementType.INSERT.equals(statementType)) {
			template.insert(queryMethod.getNamedQueryName(), parameterObject);
			return parameterObject;
		} else if (StatementType.UPDATE.equals(statementType)) {
			return template.update(queryMethod.getNamedQueryName(), parameterObject);
		} else if (StatementType.DELETE.equals(statementType)) {
			return template.delete(queryMethod.getNamedQueryName(), parameterObject);
		}
		
		throw new UnsupportedOperationException();
	}
	
	private Object parametersParsing(Object[] parameters) {
		if (isEmpty(parameters)) {
			return null;
		} else if (isSingelParameters(parameters)) {
			return parameters[0];
		} else if (hasParamAnnotation()) {
			return populateToMap(parameters);
		} else {
			// 만약 사용자가 커스텀 구현체를 만든다고 한다면 예외 발생시키는게 올바른 일인가?
			throw new IllegalArgumentException("parameter 는 기본형 다수나 하나의 Map 또는 Domain 객체만 사용할 수 있습니다. [" + Arrays.toString(parameters) + "]");
		}
	}

	private boolean isEmpty(Object[] parameters) {
        return parameters == null || parameters.length == 0 ? true : false; 
    }
    
    private boolean isSingelParameters(Object[] parameters) {
        return parameters != null && parameters.length == 1 ? true : false; 
    }
	
    private boolean hasParamAnnotation() {
    	return getQueryMethod().getClass().isAssignableFrom(AnnotationBasedSqlMapQueryMethod.class) && ((AnnotationBasedSqlMapQueryMethod) getQueryMethod()).hasParamAnnotation();
    }
    
    private Map<String, Object> populateToMap(Object[] parameters) {
		Map<String, Object> map = new HashMap<String, Object>();
		Parameters params = getQueryMethod().getParameters();
		
		for (int i = 0; i < params.getNumberOfParameters(); i++)
			map.put(params.getParameter(i).getName(), parameters[i]);
		
		return map;
	}

    @Override
	public QueryMethod getQueryMethod() {
		return queryMethod;
	}

}
