package su.rumishistem.paea.Type;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;

public class JobWorker{
	private int id;
	private ExecutorService pool;
	private AtomicReference<JobWorkerState> state = new AtomicReference<JobWorkerState>(JobWorkerState.Idle);

	public JobWorker(int id, ExecutorService pool) {
		this.id = id;
		this.pool = pool;
	}

	public JobWorkerState get_state() {
		return state.get();
	}

	public void submit(Runnable task) {
		pool.submit(new Runnable() {
			@Override
			public void run() {
				state.set(JobWorkerState.Working);

				try {
					task.run();
				} finally {
					state.set(JobWorkerState.Idle);
				}
			}
		});
	}
}
