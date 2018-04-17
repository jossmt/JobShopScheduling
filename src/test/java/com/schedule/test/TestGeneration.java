package com.schedule.test;

import com.google.common.truth.Truth;
import com.schedule.test.Config.TestDataPaths;
import com.schedule.test.Config.TestSetup;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test Schedule builder test.
 */
public class TestGeneration extends TestSetup {

    /** logger. */
    private static final Logger LOG = LoggerFactory.getLogger(TestGeneration.class);

    /**
     * Generate schedule test.
     */
    @Test
    public void generateTest() {

        final String[] benchmarks = {"ft06", "ft10", "ft20", "la01", "la10", "la11", "la23", "la34",
                "la35", "la36", "la37", "swv11", "swv13", "swv17", "swv18", "yn2", "yn3"};
        final String[] hashes = readFile(TestDataPaths.SCHEDULE_GENERATION_HASHES_PATH).split(",");

        int count = 0;
        for (final String benchmark : benchmarks) {
            setUp(benchmark, 1);

            LOG.debug("Generated hash {}, for benchmark {}", optimal.hashCode(), benchmark);
            Truth.assertThat(optimal.hashCode()).isEqualTo(Integer.valueOf(hashes[count]));

            count++;
        }
    }
}
