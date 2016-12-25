package com.github.kaklakariada.aws.sam.config;

import java.io.File;

public class ApiConfig {
	public File samTemplate;
	public File swaggerDefinition;
	public String stackBaseName;

	@Override
	public String toString() {
		return "ApiConfig [samTemplate=" + samTemplate + ", swaggerDefinition=" + swaggerDefinition + "]";
	}
}
