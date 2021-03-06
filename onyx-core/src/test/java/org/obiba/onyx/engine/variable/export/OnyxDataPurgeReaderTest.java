/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.engine.variable.export;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * Tests reading an {@link OnyxDataPurge} configuration file similar to the purge.xml file.
 */
public class OnyxDataPurgeReaderTest {
  private final String testFileName;

  private List<OnyxDataExportDestination> onyxDestinations;

  public OnyxDataPurgeReaderTest() {
    String fullClassName = this.getClass().getCanonicalName();
    Pattern p = Pattern.compile("\\.");
    Matcher m = p.matcher(fullClassName);
    testFileName = m.replaceAll("/") + ".xml";
  }

  @Before
  public void setUp() throws Exception {
    onyxDestinations = getDestinations();
  }

  @Test
  public void testNumberOfDestinations() throws Exception {
    assertThat(onyxDestinations.size(), is(1));
  }

  @Test
  public void testFirstEntityTypeName() throws Exception {
    ValueSetFilter firstValueSetFilter = onyxDestinations.get(0).getValueSetFilters().get(0);
    assertThat(firstValueSetFilter.getEntityTypeName(), is("Participant"));
  }

  @Test
  public void testEntityFiltersEnityTypeIsParticipant() throws Exception {
    ValueSetFilter firstValueSetFilter = onyxDestinations.get(0).getValueSetFilters().get(0);
    assertThat(firstValueSetFilter.getEntityFilterChain().getEntityType(), is("Participant"));
  }

  private List<OnyxDataExportDestination> getDestinations() throws IOException {
    OnyxDataPurgeReader onyxDataExportReader = new OnyxDataPurgeReader();
    System.out.println("Reading in [" + testFileName + "].");
    onyxDataExportReader.setResources(new Resource[] { new ClassPathResource(testFileName) });
    return onyxDataExportReader.read();
  }
}
