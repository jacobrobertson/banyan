package com.robestone.species.tapestry.services;

import java.util.ArrayList;
import java.util.List;

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.services.ClientInfrastructure;

public class ClientInfrastructureOverride implements ClientInfrastructure {

	public ClientInfrastructureOverride() {
	}

	public List<Asset> getJavascriptStack() {
		List<Asset> result = new ArrayList<Asset>();

		return result;
	}

	public List<Asset> getStylesheetStack() {
		List<Asset> result = new ArrayList<Asset>();
		return result;
	}
}
