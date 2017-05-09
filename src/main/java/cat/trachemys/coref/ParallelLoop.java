package cat.trachemys.coref;

import java.util.concurrent.RecursiveAction;

/**
 * 
 * http://www.omsn.de/blog/how-to-parallelize-loops-with-java-7-fork-join-framework
 * @author cristinae
 * @since 09.05.2017
 */
class ParallelLoop  extends RecursiveAction {
	
	private static final long serialVersionUID = -5942647020983098660L;
	private String fileName;

	// you can fine-tune this,
	// should be sth between 100 and 10000
	public final static int TASK_LEN = 5000;

	public ParallelLoop(String fileName) {
		this.fileName = fileName;
	}

	@Override
	protected void compute() {
/*		int len = to - from;
		if (len < TASK_LEN) {
			work(array, from, to);
		} else {
			// split work in half, execute sub-tasks asynchronously
			int mid = (from + to) >>> 1;
			new ForEach(array, from, mid).fork();
			new ForEach(array, mid, to).fork();
		}*/
}
}
