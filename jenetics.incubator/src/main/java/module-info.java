/*
 * Java Genetic Algorithm Library (@__identifier__@).
 * Copyright (c) @__year__@ Franz Wilhelmstötter
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
 *
 * Author:
 *    Franz Wilhelmstötter (franz.wilhelmstoetter@gmail.com)
 */

/**
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 * @since 8.1
 */
@SuppressWarnings("module")
module io.jenetics.incubator {
	requires io.jenetics.base;
	requires io.jenetics.ext;
	requires io.jenetics.prog;

	requires java.desktop;
	requires java.net.http;

	requires org.apache.commons.math4.legacy;
	requires org.apache.commons.statistics.distribution;
    requires org.apache.commons.numbers.gamma;
	requires com.fasterxml.jackson.databind;
	//requires swagger.parser.v3;
	//requires swagger.parser.core;

	exports io.jenetics.incubator.beans;
	exports io.jenetics.incubator.beans.description;
	exports io.jenetics.incubator.beans.property;
	exports io.jenetics.incubator.beans.reflect;
	exports io.jenetics.incubator.combinatorial;
	exports io.jenetics.incubator.csv;
	exports io.jenetics.incubator.math;
	exports io.jenetics.incubator.prog;
	exports io.jenetics.incubator.restful;
	exports io.jenetics.incubator.restful.api;
	exports io.jenetics.incubator.restful.client;
	exports io.jenetics.incubator.stat;
	exports io.jenetics.incubator.util;
}
