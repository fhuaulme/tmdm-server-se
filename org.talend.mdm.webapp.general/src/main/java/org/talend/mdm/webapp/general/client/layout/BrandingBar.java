/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.general.client.layout;

import java.util.List;

import org.talend.mdm.webapp.base.client.SessionAwareAsyncCallback;
import org.talend.mdm.webapp.base.client.util.UrlUtil;
import org.talend.mdm.webapp.base.client.util.UserContextUtil;
import org.talend.mdm.webapp.general.client.General;
import org.talend.mdm.webapp.general.client.GeneralServiceAsync;
import org.talend.mdm.webapp.general.client.i18n.MessageFactory;
import org.talend.mdm.webapp.general.model.LanguageBean;
import org.talend.mdm.webapp.general.model.ProductInfo;
import org.talend.mdm.webapp.general.model.UserBean;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ListBox;

public class BrandingBar extends ContentPanel {

    private static BrandingBar instance;

    private FlowPanel bar = new FlowPanel();

    private HorizontalPanel hp1 = new HorizontalPanel();

    private HorizontalPanel hp2 = new HorizontalPanel();

    private HTML versionLabel = new HTML();

    private ListBox languageBox = new ListBox();

    private int languageSeleted = 1;

    Button logout = new Button(MessageFactory.getMessages().logout());

    GeneralServiceAsync service = (GeneralServiceAsync) Registry.get(General.OVERALL_SERVICE);

    private BrandingBar() {
        super();
        setHeaderVisible(false);
        setBorders(false);
        buildBar();
        initEvent();
    }

    public static BrandingBar getInstance() {
        if (instance == null) {
            instance = new BrandingBar();
        }
        return instance;
    }

    private void initEvent() {

        languageBox.addChangeHandler(new ChangeHandler() {

            @Override
            public void onChange(ChangeEvent event) {
                service.setDefaultLanguage(languageBox.getValue(languageBox.getSelectedIndex()), new SessionAwareAsyncCallback<Void>() {

                    @Override
                    public void onSuccess(Void result) {
                        String path = Location.getPath();
                        String query = Location.getQueryString();
                        String lang = Location.getParameter("language"); //$NON-NLS-1$

                        if (lang == null || lang.trim().length() == 0) {
                            if (query == null || query.length() == 0) {
                                setHref(path + "?language=" + languageBox.getValue(languageBox.getSelectedIndex())); //$NON-NLS-1$
                            } else {
                                setHref(path + query + "&language=" + languageBox.getValue(languageBox.getSelectedIndex())); //$NON-NLS-1$
                            }
                        } else {

                            if (query.indexOf("&language=" + lang + "&") != -1) { //$NON-NLS-1$ //$NON-NLS-2$
                                query = query.replace("&language=" + lang + "&", "&"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                            } else if (query.endsWith("&language=" + lang)) { //$NON-NLS-1$
                                query = query.replace("&language=" + lang, ""); //$NON-NLS-1$ //$NON-NLS-2$
                            } else if (query.startsWith("?language=" + lang)) { //$NON-NLS-1$
                                query = query.replaceAll("language=" + lang + "&?", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                            }
                            setHref(path + query + "&language=" + languageBox.getValue(languageBox.getSelectedIndex())); //$NON-NLS-1$     
                        }
                    }
                });
            }
        });

        languageBox.addFocusHandler(new FocusHandler() {

            @Override
            public void onFocus(FocusEvent event) {
                if (languageBox != null) {
                    languageSeleted = languageBox.getSelectedIndex();
                }

                String languageSelected = languageBox.getValue(languageBox.getSelectedIndex());
                if (languageSelected == null || "".equals(languageSelected.trim())) { //$NON-NLS-1$
                    languageSelected = UrlUtil.getLanguage();
                }
            }
        });

        logout.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                GeneralServiceAsync service = (GeneralServiceAsync) Registry.get(General.OVERALL_SERVICE);
                service.logout(new SessionAwareAsyncCallback<String>() {

                    @Override
                    public void onSuccess(String redirectUrl) {
                        Cookies.removeCookie("JSESSIONID"); //$NON-NLS-1$
                        Cookies.removeCookie("JSESSIONIDSSO"); //$NON-NLS-1$
                        Window.Location.replace(GWT.getHostPageBaseURL() + redirectUrl);
                    }
                });
            }
        });
    }

    private native void setHref(String href)/*-{
		$wnd.location.href = href;
    }-*/;

    public void setProductInfo(ProductInfo info) {
        if (info != null && info.getProductName() != null) {
            versionLabel.setTitle(info.getProductVersion());
            versionLabel.setHTML(info.getProductName() + " " + info.getProductEdition()); //$NON-NLS-1$
        } else {
            UserBean userBean = Registry.get(General.USER_BEAN);
            versionLabel.setHTML(userBean.isEnterprise() ? MessageFactory.getMessages().enterprise() : MessageFactory
                    .getMessages().community());
        }
    }

    private void buildBar() {
        UserBean userBean = Registry.get(General.USER_BEAN);

        hp1.getElement().getStyle().setProperty("position", "absolute"); //$NON-NLS-1$ //$NON-NLS-2$
        hp2.getElement().getStyle().setProperty("top", "0px"); //$NON-NLS-1$ //$NON-NLS-2$
        hp1.getElement().getStyle().setProperty("left", "10px"); //$NON-NLS-1$ //$NON-NLS-2$
        hp1.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

        Image logoTalend = new Image("secure/img/header-back-talend.png"); //$NON-NLS-1$
        logoTalend.setHeight("28px");
        hp1.add(logoTalend);
        hp1.add(new HTML("&nbsp;&nbsp;&nbsp;&nbsp;")); //$NON-NLS-1$
        hp1.add(new Image("secure/img/header-back.png")); //$NON-NLS-1$
        hp1.add(new HTML("&nbsp;&nbsp;&nbsp;&nbsp;")); //$NON-NLS-1$
        Image logoMDM = new Image("secure/img/header-back-mdm.png"); //$NON-NLS-1$
        logoMDM.setHeight("28px");
        hp1.add(logoMDM);
        hp1.add(new HTML("&nbsp;&nbsp;")); //$NON-NLS-1$
        
        if (userBean.isEnterprise()) {
            versionLabel.setStyleName("version-label help"); //$NON-NLS-1$
        } else {
            versionLabel.setStyleName("version-label"); //$NON-NLS-1$
        }
        hp1.add(versionLabel);
        bar.add(hp1);

        hp2.getElement().getStyle().setProperty("position", "absolute"); //$NON-NLS-1$ //$NON-NLS-2$
        hp2.getElement().getStyle().setProperty("top", "5px"); //$NON-NLS-1$ //$NON-NLS-2$
        hp2.getElement().getStyle().setProperty("right", "1px"); //$NON-NLS-1$ //$NON-NLS-2$
        hp2.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        
        String html = userBean.getName();
        HTML userLabel = new HTML(html);
        userLabel.setStyleName("username"); //$NON-NLS-1$
        userLabel.getElement().setId("username-div"); //$NON-NLS-1$
        hp2.add(userLabel);      
        hp2.add(new HTML("&nbsp;&nbsp;")); //$NON-NLS-1$

        languageBox.getElement().setId("languageSelect"); //$NON-NLS-1$
        languageBox.setStyleName("language-box"); //$NON-NLS-1$
        // Enforce height
        languageBox.setHeight("20px"); //$NON-NLS-1$
        hp2.add(languageBox);
        hp2.add(new HTML("&nbsp;&nbsp;")); //$NON-NLS-1$
        
        logout.setId("btn-logout"); //$NON-NLS-1$
        hp2.add(logout);
        hp2.add(new HTML("&nbsp;&nbsp;")); //$NON-NLS-1$

        bar.setSize("100%", "100%"); //$NON-NLS-1$ //$NON-NLS-2$
        bar.setStyleName("generic-header-background"); //$NON-NLS-1$
        bar.add(hp2);

        this.add(bar);

    }

    public void buildLanguage(List<LanguageBean> languages) {
        String language = languages.get(0).getValue();
        String dateFormat = languages.get(0).getDateTimeFormat();
        for (LanguageBean lang : languages) {
            languageBox.addItem(lang.getText(), lang.getValue());
            if (lang.isSelected()) {
                language = lang.getValue();
                dateFormat = lang.getDateTimeFormat();
                languageBox.setSelectedIndex(languageBox.getItemCount() - 1);
            }
        }
        UrlUtil.setCurrentLocale(language);
        UserContextUtil.setLanguage(language);
        UserContextUtil.setDateTimeFormat(dateFormat);
    }
}