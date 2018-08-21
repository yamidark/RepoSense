package reposense.system.commands;

import java.util.Date;
import java.util.List;

import reposense.model.Author;

/**
 * Command for running the 'git log' on command line.
 */
public class GitLogCommand extends SystemCommand {
    public static final String GIT_LOG_COMMAND_WORD = "git log";

    // ignore check against email
    private static final String AUTHOR_NAME_PATTERN = "^%s <.*>$";
    private static final String DEFAULT_OUTPUT_FORMAT = "--pretty=format:\"%H|%aN|%ad|%s\" --date=iso --shortstat";

    private boolean ignoreMerges = true;

    private List<String> ignoreGlobList;
    private List<String> formats;
    private Author authorToFilter;
    private Date sinceDate;
    private Date untilDate;

    public GitLogCommand(String path) {
        super(path);
    }

    public GitLogCommand setIgnoreGlobList(List<String> ignoreGlobList) {
        this.ignoreGlobList = ignoreGlobList;
        return this;
    }

    public GitLogCommand setFormats(List<String> formats) {
        this.formats = formats;
        return this;
    }

    public GitLogCommand setAuthorFilter(Author authorToFilter) {
        this.authorToFilter = authorToFilter;
        return this;
    }

    public GitLogCommand setSinceDate(Date sinceDate) {
        this.sinceDate = sinceDate;
        return this;
    }

    public GitLogCommand setUntilDate(Date untilDate) {
        this.untilDate = untilDate;
        return this;
    }

    @Override
    protected void buildCommand() {
        command = GIT_LOG_COMMAND_WORD + " " + DEFAULT_OUTPUT_FORMAT;

        if (ignoreMerges) {
            command += " --no merges";
        }

        if (sinceDate != null) {
            command += " --since=" + addQuote(GIT_SINCE_DATE_FORMAT.format(sinceDate));
        }

        if (untilDate != null) {
            command += " --until=" + addQuote(GIT_UNTIL_DATE_FORMAT.format(untilDate));
        }

        if (authorToFilter != null) {
            command += getFilterAuthorArgs();
        }

        if (formats != null) {
            command += getGitIncludeFormatsArgs();
        }

        if (ignoreGlobList != null) {
            command += getGitExcludeGlobArgs();
        }
    }

    /**
     * Returns the {@code String} command to specify the authors to analyze for `git log` command.
     */
    private String getFilterAuthorArgs() {
        StringBuilder filterAuthorArgsBuilder = new StringBuilder(" --author=\"");

        // git author names may contain regex meta-characters, so we need to escape those
        authorToFilter.getAuthorAliases().stream()
                .map(authorAlias -> String.format(
                        AUTHOR_NAME_PATTERN, escapeSpecialRegexChars(authorAlias)) + OR_OPERATOR_PATTERN)
                .forEach(filterAuthorArgsBuilder::append);

        filterAuthorArgsBuilder.append(String.format(AUTHOR_NAME_PATTERN, authorToFilter.getGitId())).append("\"");
        return filterAuthorArgsBuilder.toString();
    }

    /**
     * Returns the {@code String} command to specify the file formats to analyze for `git` commands.
     */
    private String getGitIncludeFormatsArgs() {
        StringBuilder gitFormatsArgsBuilder = new StringBuilder();
        final String cmdFormat = " -- " + addQuote("*.%s");
        formats.stream()
                .map(format -> String.format(cmdFormat, format))
                .forEach(gitFormatsArgsBuilder::append);

        return gitFormatsArgsBuilder.toString();
    }

    /**
     * Returns the {@code String} command to specify the globs to exclude for `git log` command.
     */
    private String getGitExcludeGlobArgs() {
        StringBuilder gitExcludeGlobArgsBuilder = new StringBuilder();
        final String cmdFormat = " " + addQuote(":(exclude)%s");
        ignoreGlobList.stream()
                .filter(item -> !item.isEmpty())
                .map(ignoreGlob -> String.format(cmdFormat, ignoreGlob))
                .forEach(gitExcludeGlobArgsBuilder::append);

        return gitExcludeGlobArgsBuilder.toString();
    }
}
