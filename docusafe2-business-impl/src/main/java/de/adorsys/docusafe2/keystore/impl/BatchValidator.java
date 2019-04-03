package de.adorsys.docusafe2.keystore.impl;

import java.util.List;
import java.util.stream.Collectors;

public class BatchValidator {

	public static List<String> filterNull(List<KeyValue> input){
		return input.stream()
			.filter(l -> l.isNull())
			.map(l -> l.getKey())
			.collect(Collectors.toList());
	}
}
