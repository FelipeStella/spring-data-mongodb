/*
 * Copyright 2011-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.mongodb.core.mapping;

import java.math.BigInteger;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import org.bson.BsonObjectId;
import org.bson.types.Binary;
import org.bson.types.CodeWScope;
import org.bson.types.ObjectId;
import org.springframework.data.mapping.model.SimpleTypeHolder;
import org.springframework.data.mongodb.util.MongoClientVersion;
import org.springframework.util.ClassUtils;

import com.mongodb.DBRef;

/**
 * Simple constant holder for a {@link SimpleTypeHolder} enriched with Mongo specific simple types.
 *
 * @author Oliver Gierke
 * @author Christoph Strobl
 */
public abstract class MongoSimpleTypes {

	public static final Set<Class<?>> AUTOGENERATED_ID_TYPES;

	static {
		Set<Class<?>> classes = new HashSet<Class<?>>();
		classes.add(ObjectId.class);
		classes.add(String.class);
		classes.add(BigInteger.class);
		AUTOGENERATED_ID_TYPES = Collections.unmodifiableSet(classes);

		Set<Class<?>> simpleTypes = new HashSet<Class<?>>();
		simpleTypes.add(DBRef.class);
		simpleTypes.add(ObjectId.class);
		simpleTypes.add(BsonObjectId.class);
		simpleTypes.add(CodeWScope.class);
		simpleTypes.add(org.bson.Document.class);
		simpleTypes.add(Pattern.class);
		simpleTypes.add(Binary.class);
		simpleTypes.add(UUID.class);

		if (MongoClientVersion.isMongo34Driver()) {
			simpleTypes
					.add(ClassUtils.resolveClassName("org.bson.types.Decimal128", MongoSimpleTypes.class.getClassLoader()));
		}

		MONGO_SIMPLE_TYPES = Collections.unmodifiableSet(simpleTypes);
	}

	private static final Set<Class<?>> MONGO_SIMPLE_TYPES;
	public static final SimpleTypeHolder HOLDER = new SimpleTypeHolder(MONGO_SIMPLE_TYPES, true);

	private MongoSimpleTypes() {}
}
