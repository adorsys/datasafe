package de.adorsys.docusafe2.business.impl.keystore.generator;

import java.util.ArrayList;
import java.util.List;

public class ListOfKeyValueBuilder {
	private List<KeyValue> list = new ArrayList<>();
	
	private ListOfKeyValueBuilder() {
	}

	public ListOfKeyValueBuilder add(String key, Object value){
		list.add(new KeyValue(key, value));
		return this;
	}
	
	public static ListOfKeyValueBuilder newBuilder(){
		return new ListOfKeyValueBuilder();
	}
	
	public List<KeyValue> build(){
		return list;
	}
}
