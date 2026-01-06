package su.rumishistem.paea.Type;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class Job {
	private String id;
	private JobStatus status = JobStatus.Waiting;
	private ThrowRunnable task;
	private AtomicReference<String> message = new AtomicReference<String>("");
	private Exception ex;

	public Job() {
		this.id = UUID.randomUUID().toString();
	}

	public boolean run() {
		try {
			task.run();
			status = JobStatus.Success;
			return true;
		} catch (Exception ex) {
			this.ex = ex;
			status = JobStatus.Error;
			return false;
		}
	}

	public void set_task(ThrowRunnable task) {
		this.task = task;
	}

	public String get_id() {
		return id;
	}

	public JobStatus get_status() {
		return status;
	}

	public void set_message(String m) {
		message.set(m);
	}

	public String get_message() {
		return message.get();
	}

	public Exception get_exception() {
		return ex;
	}
}
