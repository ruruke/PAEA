package su.rumishistem.paea;

import static su.rumishistem.rumi_java_lib.LOG_PRINT.Main.LOG;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import su.rumishistem.paea.Type.Job;
import su.rumishistem.paea.Type.JobStatus;
import su.rumishistem.paea.Type.JobWorker;
import su.rumishistem.rumi_java_lib.LOG_PRINT.LOG_TYPE;

public class JobSystem {
	private final static int MAX_JOB_WORKER = 10;
	private ExecutorService pool;
	private JobWorker[] worker_list = new JobWorker[MAX_JOB_WORKER];
	private List<Job> job_list = new ArrayList<Job>();
	private AtomicInteger success_job_size = new AtomicInteger(0);
	private AtomicInteger error_job__size = new AtomicInteger(0);
	private String[] job_worker_table = new String[MAX_JOB_WORKER];

	public JobSystem() {
		pool = Executors.newFixedThreadPool(MAX_JOB_WORKER);

		for (int i = 0; i < MAX_JOB_WORKER; i++) {
			JobWorker w = new JobWorker(i, pool);
			worker_list[i] = w;
		}
	}

	public void submit(Job job) {
		job_list.add(job);
	}

	public void start() {
		//ジョブをジョブワーカーに割当して起動。
		int i = 0;
		for (Job job:job_list) {
			final int j = i;
			worker_list[i].submit(new Runnable() {
				@Override
				public void run() {
					job_worker_table[j] = job.get_id();

					boolean success = job.run();
					if (success) {
						success_job_size.incrementAndGet();
					} else {
						error_job__size.incrementAndGet();
					}

					job_worker_table[j] = null;
				}
			});

			i += 1;
			if (i >= MAX_JOB_WORKER) {
				i = 0;
			}
		}

		new Thread(new Runnable() {
			@Override
			public void run() {
				final int PROGRESS_BAR_SIZE = 64;
				final int max_job_size = job_list.size();

				try {
					while (true) {
						int end_job_count = success_job_size.get() + error_job__size.get();

						//リセット
						clear_display();

						//進捗バー
						final int progress = (int)((double) end_job_count / max_job_size * PROGRESS_BAR_SIZE);
						System.out.print("\u001B[46m");
						for (int j = 0; j < progress; j++) {
							System.out.print(" ");
						}
						System.out.print("\u001B[47m");
						for (int j = 0; j < PROGRESS_BAR_SIZE - progress; j++) {
							System.out.print(" ");
						}
						System.out.println("\u001B[0m");

						//ステータス
						System.out.println("\u001B[42m成功：" + String.format("%03d", success_job_size.get()) + "\u001B[0m");
						System.out.println("\u001B[41m失敗：" + String.format("%03d", error_job__size.get()) + "\u001B[0m");

						//ジョブワーカーの状態表示
						for (int i = 0; i < worker_list.length; i++) {
							JobWorker worker = worker_list[i];
							String status = "";
							switch (worker.get_state()) {
								case Idle: status = "サボってます"; break;
								case Working: status = "労働中"; break;
							}

							String message = "こゃーん";
							if (job_worker_table[i] != null) {
								for (Job job:job_list) {
									if (job.get_id().equals(job_worker_table[i])) {
										message = job.get_message();
										break;
									}
								}
							}

							System.out.println("["+i+"] " + status + "：" + message);
						}

						//ジョブを全て処理したか？
						if (max_job_size < end_job_count + 1) {
							pool.shutdown();
							clear_display();

							System.out.println("");
							System.out.println("-----------------------------------------------------");
							for (Job job:job_list) {
								if (job.get_status() == JobStatus.Success) {
									//LOG(LOG_TYPE.OK, "\u001B[32m" + job.get_message() + "\u001B[0m");
									LOG(LOG_TYPE.OK, job.get_message());
								} else {
									LOG(LOG_TYPE.FAILED, "\u001B[31m" + job.get_exception() + "\u001B[0m");
								}
								
							}
							System.out.println("-----------------------------------------------------");
							System.out.println(max_job_size + "個を処理しました。");

							return;
						}

						Thread.sleep(500);
					}
				} catch (InterruptedException ex) {
					//
				}
			}
		}).start();
	}

	private void clear_display() {
		System.out.print("\u001B[" + MAX_JOB_WORKER + "F");
		System.out.print("\u001B[3F");
		System.out.print("\u001B[0J");
	}
}
