package hello;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;

public class HelloHandler implements RequestStreamHandler {

	@Override
	public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
		context.getLogger().log("Hello world");
		String result = "{"
				+ "\"statusCode\":\"200\","
				+ "\"body\":\"Hello world!\","
				+ "\"headers\":{}"
				+ "}";
		output.write(result.getBytes());
	}
}