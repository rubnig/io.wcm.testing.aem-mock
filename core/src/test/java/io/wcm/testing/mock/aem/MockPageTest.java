/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2014 wcm.io
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package io.wcm.testing.mock.aem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.day.cq.commons.Filter;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.wcm.api.NameConstants;
import com.day.cq.wcm.api.Page;

import io.wcm.testing.mock.aem.context.TestAemContext;
import io.wcm.testing.mock.aem.junit.AemContext;

@SuppressWarnings("null")
public class MockPageTest {

  @Rule
  public AemContext context = TestAemContext.newAemContext();
  private Resource mockResource = mock(Resource.class);

  private Page page;

  @Before
  public void setUp() throws Exception {
    context.load().json("/json-import-samples/content.json", "/content/sample/en");

    Resource resource = this.context.resourceResolver().getResource("/content/sample/en");
    this.page = resource.adaptTo(Page.class);
  }

  @Test
  public void testProperties() {
    assertEquals("/content/sample/en", this.page.getPath());
    assertEquals("en", this.page.getName());
    assertEquals("English", this.page.getTitle());
    assertEquals("Sample Homepage", this.page.getPageTitle());
    assertNull(this.page.getNavigationTitle());
    assertNull(this.page.getDescription());
    assertEquals(3, this.page.getDepth());
    assertEquals(false, this.page.isHideInNav());
    assertNull(this.page.getVanityUrl());
    assertNotNull(this.page.hashCode());
  }

  @Test
  public void testPageManager() {
    assertNotNull(this.page.getPageManager());
  }

  @Test
  public void testPages() {
    assertTrue(this.page.hasChild("toolbar"));
    assertFalse(this.page.hasChild("nonExistingResource"));

    assertNull(this.page.getParent());

    Page toolbarPage = this.page.getPageManager().getPage("/content/sample/en/toolbar");
    assertNotNull(toolbarPage.getParent());
    assertNotNull(toolbarPage.getParent(1));
    assertNull(toolbarPage.getParent(2));
    assertEquals("/content/sample/en", toolbarPage.getAbsoluteParent(2).getPath());
    assertNull(toolbarPage.getAbsoluteParent(1));
  }

  @Test
  public void testGetAbsoluteParent() {
    // contract: http://dev.day.com/docs/en/cq/current/javadoc/com/day/cq/wcm/api/Page.html#getAbsoluteParent%28int%29

    context.create().page("/content2");
    context.create().page("/content2/sample");
    context.create().page("/content2/sample/en");
    Page testPage = context.create().page("/content2/sample/en/products");

    assertEquals("/content2", testPage.getAbsoluteParent(0).getPath());
    assertEquals("/content2/sample", testPage.getAbsoluteParent(1).getPath());
    assertEquals("/content2/sample/en", testPage.getAbsoluteParent(2).getPath());
    assertEquals("/content2/sample/en/products", testPage.getAbsoluteParent(3).getPath());
    assertNull(testPage.getAbsoluteParent(4));
  }

  @Test
  public void testGetAbsoluteParent_LaunchOldStructure() {
    context.create().page("/content/launches/launch1", "/apps/sample/templates/template1",
        "sling:resourceType", "wcm/launches/components/launch");
    context.create().page("/content/launches/launch1/content2");
    context.create().page("/content/launches/launch1/content2/sample");
    context.create().page("/content/launches/launch1/content2/sample/en");
    Page testPage = context.create().page("/content/launches/launch1/content2/sample/en/products");

    assertEquals("/content/launches/launch1/content2", testPage.getAbsoluteParent(0).getPath());
    assertEquals("/content/launches/launch1/content2/sample", testPage.getAbsoluteParent(1).getPath());
    assertEquals("/content/launches/launch1/content2/sample/en", testPage.getAbsoluteParent(2).getPath());
    assertEquals("/content/launches/launch1/content2/sample/en/products", testPage.getAbsoluteParent(3).getPath());
    assertNull(testPage.getAbsoluteParent(4));
  }

  @Test
  public void testGetAbsoluteParent_LaunchNewStructure() {
    context.create().page("/content/launches/2017/01/05/launch1", "/apps/sample/templates/template1",
        "sling:resourceType", "wcm/launches/components/launch");
    context.create().page("/content/launches/2017/01/05/launch1/content2");
    context.create().page("/content/launches/2017/01/05/launch1/content2/sample");
    context.create().page("/content/launches/2017/01/05/launch1/content2/sample/en");
    Page testPage = context.create().page("/content/launches/2017/01/05/launch1/content2/sample/en/products");

    assertEquals("/content/launches/2017/01/05/launch1/content2", testPage.getAbsoluteParent(0).getPath());
    assertEquals("/content/launches/2017/01/05/launch1/content2/sample", testPage.getAbsoluteParent(1).getPath());
    assertEquals("/content/launches/2017/01/05/launch1/content2/sample/en", testPage.getAbsoluteParent(2).getPath());
    assertEquals("/content/launches/2017/01/05/launch1/content2/sample/en/products", testPage.getAbsoluteParent(3).getPath());
    assertNull(testPage.getAbsoluteParent(4));
  }

  @Test
  public void testContentResource() {
    assertEquals("/content/sample/en/jcr:content", this.page.getContentResource().getPath());
    assertNotNull(this.page.getContentResource("par"));
    assertNull(this.page.getContentResource("nonExistingResource"));
    assertTrue(this.page.hasContent());
  }

  @Test
  public void testValidityNoOnOffTime() {
    assertNull(this.page.getOnTime());
    assertNull(this.page.getOffTime());
    assertTrue(this.page.isValid());
    assertEquals(0L, this.page.timeUntilValid());
  }

  @Test
  public void testValidityInOnOffTime() throws PersistenceException {

    // set on/off-times
    Calendar onTime = Calendar.getInstance();
    onTime.add(Calendar.DAY_OF_MONTH, -1);
    Calendar offTime = Calendar.getInstance();
    offTime.add(Calendar.DAY_OF_MONTH, 1);
    ModifiableValueMap props = this.page.getContentResource().adaptTo(ModifiableValueMap.class);
    props.put(NameConstants.PN_ON_TIME, onTime);
    props.put(NameConstants.PN_OFF_TIME, offTime);
    this.context.resourceResolver().commit();

    // Validate
    assertEquals(onTime.getTime(), this.page.getOnTime().getTime());
    assertEquals(offTime.getTime(), this.page.getOffTime().getTime());
    assertTrue(this.page.isValid());
    assertEquals(0L, this.page.timeUntilValid());
  }

  @Test
  public void testValidityOnTimeInFuture() throws PersistenceException {

    // set on/off-times
    Calendar onTime = Calendar.getInstance();
    onTime.add(Calendar.DAY_OF_MONTH, 1);
    Calendar offTime = Calendar.getInstance();
    offTime.add(Calendar.DAY_OF_MONTH, 2);
    ModifiableValueMap props = this.page.getContentResource().adaptTo(ModifiableValueMap.class);
    props.put(NameConstants.PN_ON_TIME, onTime);
    props.put(NameConstants.PN_OFF_TIME, offTime);
    this.context.resourceResolver().commit();

    // Validate
    assertEquals(onTime.getTime(), this.page.getOnTime().getTime());
    assertEquals(offTime.getTime(), this.page.getOffTime().getTime());
    assertFalse(this.page.isValid());
    assertTrue(this.page.timeUntilValid() > 0L);
  }

  @Test
  public void testValidityOnTimeInPast() throws PersistenceException {

    // set on/off-times
    Calendar onTime = Calendar.getInstance();
    onTime.add(Calendar.DAY_OF_MONTH, -2);
    Calendar offTime = Calendar.getInstance();
    offTime.add(Calendar.DAY_OF_MONTH, -1);
    ModifiableValueMap props = this.page.getContentResource().adaptTo(ModifiableValueMap.class);
    props.put(NameConstants.PN_ON_TIME, onTime);
    props.put(NameConstants.PN_OFF_TIME, offTime);
    this.context.resourceResolver().commit();

    // Validate
    assertEquals(onTime.getTime(), this.page.getOnTime().getTime());
    assertEquals(offTime.getTime(), this.page.getOffTime().getTime());
    assertFalse(this.page.isValid());
    assertTrue(this.page.timeUntilValid() < 0L);
  }

  @Test
  public void testLastModified() {
    assertNotNull(this.page.getLastModifiedBy());
    assertNotNull(this.page.getLastModified());
  }

  @Test
  public void testTemplate() {
    this.context.load().json("/json-import-samples/application.json", "/apps/sample");
    assertNotNull(this.page.getTemplate());
  }

  @Test
  public void testListChildren() {
    List<Page> childPages = IteratorUtils.toList(this.page.listChildren());
    assertEquals(1, childPages.size());
  }

  @Test
  public void testListChildrenFiltered() {
    List<Page> childPages = IteratorUtils.toList(this.page.listChildren(new Filter<Page>() {
      @Override
      public boolean includes(final Page element) {
        return !StringUtils.equals("toolbar", element.getName());
      }
    }));
    assertEquals(0, childPages.size());
  }

  @Test
  public void testListChildrenFilteredDeep() {
    List<Page> childPages = IteratorUtils.toList(this.page.listChildren(new Filter<Page>() {
      @Override
      public boolean includes(final Page element) {
        return !StringUtils.equals("toolbar", element.getName());
      }
    }, true));
    assertEquals(1, childPages.size());
  }

  @Test
  public void testAdaptTo() {
    Page underTest = new MockPage(mockResource);

    Resource resource = underTest.adaptTo(Resource.class);
    assertEquals(mockResource, resource);
  }

  @Test
  public void testGetLanguage() {
    Page profilesPage = context.pageManager().getPage("/content/sample/en/toolbar/profiles");

    // set language in site root
    ModifiableValueMap props = this.page.getContentResource().adaptTo(ModifiableValueMap.class);
    props.put(JcrConstants.JCR_LANGUAGE, "fr_FR");

    // test get language from content
    Locale locale = profilesPage.getLanguage(false);
    assertEquals(Locale.forLanguageTag("fr-FR"), locale);

    // test get language from path
    locale = profilesPage.getLanguage(true);
    assertEquals(Locale.forLanguageTag("en"), locale);
  }

  @Test
  public void testEquals() throws Exception {
    Page page1 = context.pageManager().getPage("/content/sample/en");
    Page page2 = context.pageManager().getPage("/content/sample/en");
    Page page3 = context.pageManager().getPage("/content/sample/en/toolbar/profiles");

    assertTrue(page1.equals(page2));
    assertFalse(page1.equals(page3));
  }

}
