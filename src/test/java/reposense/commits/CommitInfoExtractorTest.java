package reposense.commits;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import reposense.commits.model.CommitInfo;
import reposense.model.Author;
import reposense.template.GitTestTemplate;
import reposense.util.TestUtil;

public class CommitInfoExtractorTest extends GitTestTemplate {

    @Test
    public void withContentTest() {
        config.setAuthorList(Collections.singletonList(new Author(".*")));
        List<CommitInfo> commits = CommitInfoExtractor.extractCommitInfos(config);
        Assert.assertFalse(commits.isEmpty());
    }

    @Test
    public void withoutContentTest() {
        Date sinceDate = TestUtil.getDate(2050, Calendar.JANUARY, 1);
        config.setSinceDate(sinceDate);
        config.setAuthorList(Collections.singletonList(new Author(".*")));

        List<CommitInfo> commits = CommitInfoExtractor.extractCommitInfos(config);
        Assert.assertTrue(commits.isEmpty());
    }
}
