package ch.ralscha.e4ds.service;

import static ch.ralscha.extdirectspring.annotation.ExtDirectMethodType.POLL;

import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import ch.ralscha.extdirectspring.annotation.ExtDirectMethod;

@Service
@Lazy
public class PollService {

	private DateTimeFormatter fmt = DateTimeFormat.forPattern("HH:mm:ss");

	@ExtDirectMethod(value = POLL, event = "chartdata")
	@RequiresAuthentication
	public Poll getPollData() {
		return new Poll(fmt.print(new DateTime()), (int) (Math.random() * 1000));
	}
}
