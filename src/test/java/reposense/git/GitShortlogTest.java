package reposense.git;

import static reposense.git.GitShortlog.extractAuthorsFromLog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import reposense.model.Author;
import reposense.template.GitTestTemplate;
import reposense.util.TestUtil;

public class GitShortlogTest extends GitTestTemplate {

    @Test
    public void extractAuthorsFromLog_validRepoNoDateRange_success() {
        List<Author> expectedAuthorList = new ArrayList<>();
        expectedAuthorList.add(new Author("eugenepeh"));
        expectedAuthorList.add(new Author("fakeAuthor"));
        expectedAuthorList.add(new Author("harryggg"));

        List<Author> actualAuthorList = extractAuthorsFromLog(config);

        Assert.assertEquals(expectedAuthorList.size(), actualAuthorList.size());
        Assert.assertEquals(expectedAuthorList, actualAuthorList);
    }

    @Test
    public void extractAuthorsFromLog_validRepoDateRange_success() {
        List<Author> expectedAuthorList = new ArrayList<>();

        expectedAuthorList.add(new Author("eugenepeh"));
        config.setSinceDate(TestUtil.getDate(2018, Calendar.MAY, 5));
        config.setUntilDate(TestUtil.getDate(2018, Calendar.MAY, 10));

        List<Author> actualAuthorList = extractAuthorsFromLog(config);

        Assert.assertEquals(expectedAuthorList.size(), actualAuthorList.size());
        Assert.assertEquals(expectedAuthorList, actualAuthorList);
    }
}
