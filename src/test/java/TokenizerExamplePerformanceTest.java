

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.atilika.kuromoji.Token;
import org.atilika.kuromoji.Tokenizer;
import org.junit.Test;

/**
 * kuromoji の性能試験
 * @author mkobayas
 *
 */
public class TokenizerExamplePerformanceTest {

	static String[] inputs = {
		"小学校に居る時分学校の二階から飛び降りて一週間ほど腰《こし》を抜《ぬ》かした事がある。坊っちゃん夏目漱石",
		"なぜそんな無闇《むやみ》をしたと聞く人があるかも知れぬ。",
		"別段深い理由でもない。",
		"新築の二階から首を出していたら、同級生の一人が冗談《じょうだん》に、いくら威張《いば》っても、そこから飛び降りる事は出来まい。",
		"弱虫やーい。",
		"と囃《はや》したからである。",
		"小使《こづかい》に負ぶさって帰って来た時、おやじが大きな眼《め》をして二階ぐらいから飛び降りて腰を抜かす奴《やつ》があるかと云《い》ったから、この次は抜かさずに飛んで見せますと答えた。",
		"　親類のものから西洋製のナイフを貰《もら》って奇麗《きれい》な刃《は》を日に翳《かざ》して、友達《ともだち》に見せていたら、一人が光る事は光るが切れそうもないと云った。",
		"切れぬ事があるか、何でも切ってみせると受け合った。",
		"そんなら君の指を切ってみろと注文したから、何だ指ぐらいこの通りだと右の手の親指の甲《こう》をはすに切り込《こ》んだ。",
		"幸《さいわい》ナイフが小さいのと、親指の骨が堅《かた》かったので、今だに親指は手に付いている。",
		"しかし創痕《きずあと》は死ぬまで消えぬ。",
		"　庭を東へ二十歩に行き尽《つく》すと、南上がりにいささかばかりの菜園があって、真中《まんなか》に栗《くり》の木が一本立っている。",
		"これは命より大事な栗だ。",
		"実の熟する時分は起き抜けに背戸《せど》を出て落ちた奴を拾ってきて、学校で食う。",
		"菜園の西側が山城屋《やましろや》という質屋の庭続きで、この質屋に勘太郎《かんたろう》という十三四の倅《せがれ》が居た。",
		"勘太郎は無論弱虫である。",
		"弱虫の癖《くせ》に四つ目垣を乗りこえて、栗を盗《ぬす》みにくる。",
		"ある日の夕方｜折戸《おりど》の蔭《かげ》に隠《かく》れて、とうとう勘太郎を捕《つら》まえてやった。",
		"その時勘太郎は逃《に》げ路《みち》を失って、一生懸命《いっしょうけんめい》に飛びかかってきた。",
		"向《むこ》うは二つばかり年上である。",
		"弱虫だが力は強い。",
		"鉢《はち》の開いた頭を、こっちの胸へ宛《あ》ててぐいぐい押《お》した拍子《ひょうし》に、勘太郎の頭がすべって、おれの袷《あわせ》の袖《そで》の中にはいった。",
		"邪魔《じゃま》になって手が使えぬから、無暗に手を振《ふ》ったら、袖の中にある勘太郎の頭が、右左へぐらぐら靡《なび》いた。",
		"しまいに苦しがって袖の中から、おれの二の腕《うで》へ食い付いた。",
		"痛かったから勘太郎を垣根へ押しつけておいて、足搦《あしがら》をかけて向うへ倒《たお》してやった。",
		"山城屋の地面は菜園より六尺がた低い。",
		"勘太郎は四つ目垣を半分｜崩《くず》して、自分の領分へ真逆様《まっさかさま》に落ちて、ぐうと云った。",
		"勘太郎が落ちるときに、おれの袷の片袖がもげて、急に手が自由になった。",
		"その晩母が山城屋に詫《わ》びに行ったついでに袷の片袖も取り返して来た。",
		"　この外いたずらは大分やった。",
		"大工の兼公《かねこう》と肴屋《さかなや》の角《かく》をつれて、茂作《もさく》の人参畠《にんじんばたけ》をあらした事がある。",
		"人参の芽が出揃《でそろ》わぬ処《ところ》へ藁《わら》が一面に敷《し》いてあったから、その上で三人が半日｜相撲《すもう》をとりつづけに取ったら、人参がみんな踏《ふ》みつぶされてしまった。" };

	static Tokenizer tokenizer = Tokenizer.builder().build();
	
	volatile boolean runnning;
	CountDownLatch startLatch;

	@Test
	public void test() throws Exception {
		doTest("[RumpUP]", 3000);
		doTest("[TEST]", 5000);
		doTest("[TEST]", 5000);
	}
	
	public void doTest(String lable, long runningTime) throws Exception {
		runnning = true;
		
		int threads = Runtime.getRuntime().availableProcessors()*2;
		startLatch = new CountDownLatch(threads);
		
		System.out.println(lable + ": Start : threads=" + threads + ", runningTime=" + runningTime + " ms");
		
		ExecutorService executorService = Executors.newFixedThreadPool(threads);

		Runner[] runners = new Runner[threads];
		for(int i=0; i<threads; i++) {
			runners[i] = new Runner();
			executorService.execute(runners[i]);
		}
		
		startLatch.await();
		Thread.sleep(runningTime);
		runnning = false;

		long totalExecution = 0;

		for(int i=0; i<threads; i++) {
			totalExecution += runners[i].count;
		}
		
		long throughput = (long) (totalExecution / ((double)runningTime/1000));
		
		System.out.println(String.format(
				"%4$s: Running Time=%1$,d ms, Throughput=%2$,d tps, totalExecution=%3$,d exec",
				runningTime, throughput, totalExecution, lable));
		
		executorService.shutdown();
	}
	
	public class Runner implements Runnable {
		public int count = 0;
		
		@SuppressWarnings("unused")
		@Override
		public void run() {
			startLatch.countDown();
			try {
				startLatch.await();
			} catch (Exception e) {}
			
			while(true) {
				if(runnning == false) break; 
				for (String s : inputs) {
					if(runnning == false) break; 
					List<Token> result = tokenizer.tokenize(s);
//					if(result.size() < 1) return;
//					System.out.println("result.size()=" + result.size());
					count++;
				}
			}
		}
		
	}
}