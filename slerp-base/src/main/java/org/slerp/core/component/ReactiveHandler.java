package org.slerp.core.component;

import java.io.File;
import java.util.concurrent.Callable;

import org.slerp.core.CoreException;
import org.slerp.core.Domain;
import org.slerp.core.business.BusinessFunction;
import org.slerp.core.business.BusinessTransaction;
import org.slerp.core.business.DefaultBusinessTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

@Component
public class ReactiveHandler {
	static Logger log = LoggerFactory.getLogger(ReactiveHandler.class);

	public Single<Domain> deferSingle(BusinessFunction function, Domain input) {
		return Single.defer(() -> single(function, input));
	}

	public Single<Domain> deferSingle(BusinessTransaction transaction, Domain input) {
		return Single.defer(() -> single(transaction, input));
	}

	public Observable<Domain> deferObservable(BusinessFunction function, Domain input) {
		return Observable.defer(() -> observable(function, input));
	}

	public Observable<Domain> deferObservable(BusinessTransaction transaction, Domain input) {
		return Observable.defer(() -> observable(transaction, input));
	}

	public Single<Domain> single(BusinessFunction function, Domain input) {
		final Domain template = new Domain();
		// Compute body to fill the input
		return Single.just(input).observeOn(Schedulers.computation()).map(i -> {
			template.put("status", 0);
			template.put("body", i);
			return template;
		}).observeOn(Schedulers.io()).doOnSuccess(body -> {
			body.put("status", 0);
			body.put("body", function.handle(body.getDomain("body")));
		}).subscribeOn(Schedulers.io()).onErrorResumeNext(t -> {
			if (t.getClass().isAssignableFrom(CoreException.class)) {
				CoreException e = (CoreException) t;
				template.put("status", 1);
				template.put("body", e.getMessage());
				log.error("CoreException", e);
			} else {
				template.put("status", 2);
				template.put("body", t.toString());
			}
			return Single.just(template);
		});
	}

	public Single<Domain> single(BusinessTransaction transaction, Domain input) {
		final Domain template = new Domain();
		return Single.just(input).observeOn(Schedulers.computation()).map(i -> {
			template.put("status", 0);
			template.put("body", i);
			return template;
		}).observeOn(Schedulers.io()).doOnSuccess(body -> {
			body.put("status", 0);
			body.put("body", transaction.handle(body.getDomain("body")));
		}).subscribeOn(Schedulers.io()).onErrorResumeNext(t -> {
			if (t.getClass().isAssignableFrom(CoreException.class)) {
				CoreException e = (CoreException) t;
				template.put("status", 1);
				template.put("body", e.getMessage());
				log.error("CoreException", e);
			} else {
				log.error("Exception", t);
				template.put("status", 2);
				template.put("body", t.getMessage());
				template.put("cause", t.toString());
			}
			return Single.just(template);
		});
	}

	public Observable<Domain> observable(BusinessFunction function, Domain input) {
		final Domain template = new Domain();
		return Observable.just(input).observeOn(Schedulers.computation()).map(i -> {
			template.put("status", 0);
			template.put("body", i);
			return template;
		}).observeOn(Schedulers.io()).doOnNext(body -> {
			body.put("status", 0);
			body.put("body", function.handle(body.getDomain("body")));
		}).subscribeOn(Schedulers.io()).onErrorResumeNext(t -> {
			if (t instanceof CoreException) {
				CoreException e = (CoreException) t;
				template.put("status", 1);
				template.put("body", e.getMessage());
			} else {
				log.error("Exception", t);
				template.put("status", 2);
				template.put("body", t.getMessage());
				template.put("cause", t.toString());
			}
			return Observable.just(template);
		});
	}

	public Observable<Domain> observable(BusinessTransaction transaction, Domain input) {
		final Domain template = new Domain();
		return Observable.just(input).observeOn(Schedulers.computation()).map(i -> {
			template.put("status", 0);
			template.put("body", i);
			return template;
		}).observeOn(Schedulers.io()).doOnNext(body -> {
			body.put("status", 0);
			body.put("body", transaction.handle(body.getDomain("body")));
		}).onErrorResumeNext(t -> {
			if (t.getClass().isAssignableFrom(CoreException.class)) {
				CoreException e = (CoreException) t;
				template.put("status", 1);
				template.put("body", e.getMessage());
			} else {
				log.error("Exception", t);
				template.put("status", 2);
				template.put("body", t.getMessage());
				template.put("cause", t.toString());
			}
			return Observable.just(template);
		});
	}

	public Single<Domain> fix(Callable<Domain> callable) {
		final Domain template = new Domain();
		return Single.fromCallable(callable).map(handle -> {
			return callable.call().put("status", 0);
		}).onErrorResumeNext(t -> {
			if (t.getClass().isAssignableFrom(CoreException.class)) {
				CoreException e = (CoreException) t;
				template.put("status", 1);
				template.put("body", e.getMessage());
				log.error("CoreException", e);
			} else {
				log.error("Exception", t);
				template.put("status", 2);
				template.put("body", t.getMessage());
				template.put("cause", t.toString());
			}
			return Single.just(template);
		});
	}

	public static void main(String[] args) throws InterruptedException {
		Domain domain = new Domain();
		domain.put("test1", "this is test");
		BusinessTransaction transaction = new DefaultBusinessTransaction() {

			@Override
			public void prepare(Domain inputDomain) throws Exception {
				throw new CoreException("unknown.exception");
			}

			@Override
			public Domain handle(Domain inputDomain) {
				inputDomain.writeTo(new File("output.json"));
				return super.handle(inputDomain);
			}
		};
		ReactiveHandler handler = new ReactiveHandler();
		handler.single(transaction, domain).subscribe(o -> {
			System.out.print(Thread.currentThread().getName() + " ");
			System.out.println(o.toString());
		});
		Thread.sleep(2000);
	}

}
