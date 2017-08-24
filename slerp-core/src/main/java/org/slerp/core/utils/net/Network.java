/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package org.slerp.core.utils.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slerp.core.CoreException;
import org.slerp.core.Domain;
import org.slerp.core.utils.Net;
import org.slerp.core.utils.Net.HttpMethods;
import org.slerp.core.utils.Net.HttpRequest;
import org.slerp.core.utils.Net.HttpResponse;
import org.slerp.core.utils.Net.HttpResponseListener;
import org.slerp.core.utils.StreamUtils;

/**
 * Implements part of the {@link Net} API using {@link HttpURLConnection}, to be
 * easily reused between the Android and Desktop backends.
 * 
 * @author acoppes
 */
public class Network {

	static class HttpClientResponse implements HttpResponse {
		private final HttpURLConnection connection;
		private HttpStatus status;

		public HttpClientResponse(HttpURLConnection connection) throws IOException {
			this.connection = connection;
			try {
				this.status = new HttpStatus(connection.getResponseCode());
			} catch (IOException e) {
				this.status = new HttpStatus(-1);
			}
		}

		@Override
		public byte[] getResult() {
			InputStream input = getInputStream();
			try {
				return StreamUtils.copyStreamToBytes(input, connection.getContentLength());
			} catch (Exception e) {
				return StreamUtils.EMPTY_BYTES;
			} finally {
				StreamUtils.close(input);
			}
		}

		@Override
		public String getResultAsString() {
			InputStream input = getInputStream();
			try {
				return StreamUtils.copyStreamToString(input, connection.getContentLength());
			} catch (IOException e) {
				return "";
			} finally {
				StreamUtils.close(input);
			}
		}

		@Override
		public InputStream getResultAsStream() {
			return getInputStream();
		}

		@Override
		public HttpStatus getStatus() {
			return status;
		}

		@Override
		public String getHeader(String name) {
			return connection.getHeaderField(name);
		}

		@Override
		public Map<String, List<String>> getHeaders() {
			return connection.getHeaderFields();
		}

		private InputStream getInputStream() {
			try {
				return connection.getInputStream();
			} catch (IOException e) {
				return connection.getErrorStream();
			}
		}

		@Override
		public Domain getResultAsDomain() {
			return new Domain(getResultAsString());
		}
	}

	private final ExecutorService executorService;
	final HashMap<HttpRequest, HttpURLConnection> connections;
	final HashMap<HttpRequest, HttpResponseListener> listeners;
	final Lock lock;

	public Network() {
		executorService = Executors.newCachedThreadPool();
		connections = new HashMap<HttpRequest, HttpURLConnection>();
		listeners = new HashMap<HttpRequest, HttpResponseListener>();
		lock = new ReentrantLock();
	}
	
	public void send(final HttpRequest httpRequest, final HttpResponseListener httpResponseListener) {
		if (httpRequest.getUrl() == null) {
			httpResponseListener.failed(new CoreException("can't process a HTTP request without URL set"));
			return;
		}

		try {
			final String method = httpRequest.getMethod();
			URL url;

			if (method.equalsIgnoreCase(HttpMethods.GET)) {
				String queryString = "";
				String value = httpRequest.getContent();
				if (value != null && !"".equals(value))
					queryString = "?" + value;
				url = new URL(httpRequest.getUrl() + queryString);
			} else {
				url = new URL(httpRequest.getUrl());
			}

			final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			// should be enabled to upload data.
			final boolean doingOutPut = method.equalsIgnoreCase(HttpMethods.POST)
					|| method.equalsIgnoreCase(HttpMethods.PUT);
			connection.setDoOutput(doingOutPut);
			connection.setDoInput(true);
			connection.setRequestMethod(method);
			HttpURLConnection.setFollowRedirects(httpRequest.getFollowRedirects());

			lock.lock();
			connections.put(httpRequest, connection);
			listeners.put(httpRequest, httpResponseListener);
			lock.unlock();

			// Headers get set regardless of the method
			for (Map.Entry<String, String> header : httpRequest.getHeaders().entrySet())
				connection.addRequestProperty(header.getKey(), header.getValue());

			// Set Timeouts
			connection.setConnectTimeout(httpRequest.getTimeOut());
			connection.setReadTimeout(httpRequest.getTimeOut());

			executorService.submit(new Runnable() {
				@Override
				public void run() {
					try {
						// Set the content for POST and PUT (GET has the
						// information embedded in the URL)
						if (doingOutPut) {
							// we probably need to use the content as stream
							// here instead of using it as a string.
							String contentAsString = httpRequest.getContent();
							if (contentAsString != null) {
								OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
								try {
									writer.write(contentAsString);
								} finally {
									StreamUtils.close(writer);
								}
							} else {
								InputStream contentAsStream = httpRequest.getContentStream();
								if (contentAsStream != null) {
									OutputStream os = connection.getOutputStream();
									try {
										StreamUtils.copyStream(contentAsStream, os);
									} finally {
										StreamUtils.close(os);
									}
								}
							}
						}

						connection.connect();

						final HttpClientResponse clientResponse = new HttpClientResponse(connection);
						try {
							lock.lock();
							HttpResponseListener listener = listeners.get(httpRequest);

							if (listener != null) {
								lock.unlock();
								listener.handleHttpResponse(clientResponse);
								lock.lock();
								listeners.remove(httpRequest);
							}

							connections.remove(httpRequest);
						} finally {
							lock.unlock();
							connection.disconnect();
						}
					} catch (final Exception e) {
						connection.disconnect();
						try {
							httpResponseListener.failed(e);
						} finally {
							lock.lock();
							connections.remove(httpRequest);
							listeners.remove(httpRequest);
							lock.unlock();
						}
					}
				}
			});
		} catch (Exception e) {
			try {
				httpResponseListener.failed(e);
			} finally {
				lock.lock();
				connections.remove(httpRequest);
				listeners.remove(httpRequest);
				lock.unlock();
			}
			return;
		} finally {
			executorService.shutdown();
		}
	}

	public void cancel(HttpRequest httpRequest) {
		try {
			lock.lock();
			HttpResponseListener httpResponseListener = listeners.get(httpRequest);

			if (httpResponseListener != null) {
				lock.unlock();
				httpResponseListener.cancelled();
				lock.lock();
				connections.remove(httpRequest);
				listeners.remove(httpRequest);
			}
		} finally {
			lock.unlock();
		}
	}

	static final public Network instances = new Network();


}
