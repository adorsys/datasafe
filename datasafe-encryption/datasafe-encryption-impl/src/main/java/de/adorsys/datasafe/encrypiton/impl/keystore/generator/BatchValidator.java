package de.adorsys.datasafe.encrypiton.impl.keystore.generator;

import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class BatchValidator {

	public static List<String> filterNull(List<KeyValue> input){
		return input.stream()
			.filter(KeyValue::isNull)
			.map(KeyValue::getKey)
			.collect(Collectors.toList());
	}
}
