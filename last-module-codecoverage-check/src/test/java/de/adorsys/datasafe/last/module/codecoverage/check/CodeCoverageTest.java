package de.adorsys.datasafe.last.module.codecoverage.check;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CodeCoverageTest {
    @Test
    public void test() {
        String content = "affe";
        CodeCoverage c = new CodeCoverage();
        c.setConent(content);
        Assertions.assertEquals(content, c.getContent());
    }
}
