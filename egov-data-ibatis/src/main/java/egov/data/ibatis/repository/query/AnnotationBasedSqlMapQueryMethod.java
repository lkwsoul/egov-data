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

import java.lang.reflect.*;

import org.springframework.data.repository.core.*;
import org.springframework.data.repository.query.*;

import egov.data.ibatis.repository.annotation.*;

/**
 * Repository 인터페이스에 있는 @Namespace 처리 전략 클래스
 * 
 * @author Yunseok Choi
 *
 */
public class AnnotationBasedSqlMapQueryMethod extends QueryMethod {
	
	private Class<?> repositoryInterface;
	private String methodName;
	
	public AnnotationBasedSqlMapQueryMethod(Method method, RepositoryMetadata metadata) {
		super(method, metadata);
		this.repositoryInterface = metadata.getRepositoryInterface();
		this.methodName = method.getName();
	}
	
	@Override
	public String getNamedQueryName() {
		Namespace namespace = repositoryInterface.getAnnotation(Namespace.class);
		return String.format("%s.%s", namespace.value(), methodName);
	}

}