/*
 * Copyright 2017 the original author or authors.
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
package org.springframework.data.mongodb.core;

import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.springframework.data.mongodb.core.ExecutableFindOperation.TerminatingFindOperation;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Query;
import spring.data.microbenchmark.AbstractMicrobenchmark;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.TearDown;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.ExecutableFindOperation.FindOperationWithQuery;
import org.springframework.data.mongodb.core.mapping.Field;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;

/**
 * @author Christoph Strobl
 */
public class ProjectionsBenchmark extends AbstractMicrobenchmark {

	private static final String DB_NAME = "projections-benchmark";
	private static final String COLLECTION_NAME = "projections";

	private MongoTemplate template;
	private MongoClient client;
	private MongoCollection<Document> mongoCollection;

	private Person source;

	private FindOperationWithQuery<Person> asPerson;
	private FindOperationWithQuery<DtoProjection> asDtoProjection;
	private FindOperationWithQuery<ClosedProjection> asClosedProjection;
	private FindOperationWithQuery<OpenProjection> asOpenProjection;

	private TerminatingFindOperation<Person> asPersonWithFieldsRestriction;
	private Document fields = new Document("firstname", 1);

	@Setup
	public void setUp() {

		client = new MongoClient(new ServerAddress());
		template = new MongoTemplate(client, DB_NAME);

		source = new Person();
		source.firstname = "luke";
		source.lastname = "skywalker";

		source.address = new Address();
		source.address.street = "melenium falcon 1";
		source.address.city = "deathstar";

		template.save(source, COLLECTION_NAME);

		asPerson = template.query(Person.class).inCollection(COLLECTION_NAME);
		asDtoProjection = template.query(Person.class).inCollection(COLLECTION_NAME).as(DtoProjection.class);
		asClosedProjection = template.query(Person.class).inCollection(COLLECTION_NAME).as(ClosedProjection.class);
		asOpenProjection = template.query(Person.class).inCollection(COLLECTION_NAME).as(OpenProjection.class);

		asPersonWithFieldsRestriction = template.query(Person.class).inCollection(COLLECTION_NAME)
				.matching(new BasicQuery(new Document(), new Document("firstname", 1)));

		mongoCollection = client.getDatabase(DB_NAME).getCollection(COLLECTION_NAME);
	}

	@TearDown
	public void tearDown() {

		client.dropDatabase(DB_NAME);
		client.close();
	}

	@Benchmark // DATAMONGO-1733
	public Object readIntoDomainType() {
		return asPerson.all();
	}

	@Benchmark // DATAMONGO-1733
	public Object readIntoDomainTypeWihtFields() {
		return asPersonWithFieldsRestriction.all();
	}

	@Benchmark // DATAMONGO-1733
	public Object readIntoDto() {
		return asDtoProjection.all();
	}

	@Benchmark // DATAMONGO-1733
	public Object readClosedProjection() {
		return asClosedProjection.all();
	}

	@Benchmark // DATAMONGO-1733
	public Object readIntoOpenProjection() {
		return asOpenProjection.all();
	}

	@Benchmark // DATAMONGO-1733
	public Object plainMongo() {
		return mongoCollection.find().first();
	}

	@Benchmark // DATAMONGO-1733
	public Object plainMongoFields() {
		return mongoCollection.find().filter(fields).first();
	}

	static class Person {

		@Id String id;
		String firstname;
		String lastname;
		Address address;
	}

	static class Address {

		String city;
		String street;
	}

	static class DtoProjection {

		@Field("firstname") String name;
	}

	static interface ClosedProjection {

		String getFirstname();
	}

	static interface OpenProjection {

		@Value("#{target.firstname}")
		String name();
	}

}