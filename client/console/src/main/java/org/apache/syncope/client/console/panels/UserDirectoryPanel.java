/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.syncope.client.console.panels;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.syncope.client.console.SyncopeConsoleSession;
import org.apache.syncope.client.console.commons.Constants;
import org.apache.syncope.client.console.notifications.NotificationTasks;
import org.apache.syncope.client.console.pages.BasePage;
import org.apache.syncope.client.console.rest.UserRestClient;
import org.apache.syncope.client.console.status.AnyStatusModal;
import org.apache.syncope.client.console.status.ChangePasswordModal;
import org.apache.syncope.client.console.tasks.AnyPropagationTasks;
import org.apache.syncope.client.console.wicket.extensions.markup.html.repeater.data.table.AttrColumn;
import org.apache.syncope.client.console.wicket.extensions.markup.html.repeater.data.table.KeyPropertyColumn;
import org.apache.syncope.client.console.wicket.markup.html.form.ActionLink;
import org.apache.syncope.client.console.wicket.markup.html.form.ActionLink.ActionType;
import org.apache.syncope.client.console.wicket.markup.html.form.ActionsPanel;
import org.apache.syncope.client.console.wizards.AjaxWizard;
import org.apache.syncope.client.console.wizards.WizardMgtPanel;
import org.apache.syncope.client.console.wizards.any.AnyWrapper;
import org.apache.syncope.client.console.wizards.any.UserWrapper;
import org.apache.syncope.common.lib.SyncopeConstants;
import org.apache.syncope.common.lib.to.AnyTypeClassTO;
import org.apache.syncope.common.lib.to.UserTO;
import org.apache.syncope.common.lib.types.AnyTypeKind;
import org.apache.syncope.common.lib.types.SchemaType;
import org.apache.syncope.common.lib.types.StandardEntitlement;
import org.apache.wicket.PageReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.springframework.util.ReflectionUtils;

public class UserDirectoryPanel extends AnyDirectoryPanel<UserTO, UserRestClient> {

    private static final long serialVersionUID = -1100228004207271270L;

    protected UserDirectoryPanel(final String id, final Builder builder) {
        this(id, builder, true);
    }

    protected UserDirectoryPanel(final String id, final Builder builder, final boolean wizardInModal) {
        super(id, builder, wizardInModal);

        altDefaultModal.setWindowClosedCallback(new ModalWindow.WindowClosedCallback() {

            private static final long serialVersionUID = 8804221891699487139L;

            @Override
            public void onClose(final AjaxRequestTarget target) {
                updateResultTable(target);
                modal.show(false);
            }
        });
    }

    @Override
    protected String paginatorRowsKey() {
        return Constants.PREF_USERS_PAGINATOR_ROWS;
    }

    @Override
    protected Collection<ActionType> getBulkActions() {
        List<ActionType> bulkActions = new ArrayList<>();

        bulkActions.add(ActionType.MUSTCHANGEPASSWORD);
        bulkActions.add(ActionType.DELETE);
        bulkActions.add(ActionType.SUSPEND);
        bulkActions.add(ActionType.REACTIVATE);

        return bulkActions;
    }

    @Override
    public ActionsPanel<Serializable> getHeader(final String componentId) {
        final ActionsPanel<Serializable> panel = super.getHeader(componentId);

        panel.add(new ActionLink<Serializable>() {

            private static final long serialVersionUID = -7978723352517770644L;

            @Override
            public void onClick(final AjaxRequestTarget target, final Serializable ignore) {
                target.add(displayAttributeModal.setContent(new UserDisplayAttributesModalPanel<>(
                        displayAttributeModal, page.getPageReference(), pSchemaNames, dSchemaNames)));

                displayAttributeModal.header(new ResourceModel("any.attr.display"));
                displayAttributeModal.addSubmitButton();
                displayAttributeModal.show(true);
            }

            @Override
            protected boolean statusCondition(final Serializable modelObject) {
                return wizardInModal;
            }
        }, ActionType.CHANGE_VIEW, StandardEntitlement.USER_READ).hideLabel();
        return panel;
    }

    @Override
    protected List<IColumn<UserTO, String>> getColumns() {
        final List<IColumn<UserTO, String>> columns = new ArrayList<>();
        final List<IColumn<UserTO, String>> prefcolumns = new ArrayList<>();

        columns.add(new KeyPropertyColumn<UserTO>(
                new ResourceModel(Constants.KEY_FIELD_NAME, Constants.KEY_FIELD_NAME), Constants.KEY_FIELD_NAME));

        for (String name : prefMan.getList(getRequest(), Constants.PREF_USERS_DETAILS_VIEW)) {
            if (!Constants.KEY_FIELD_NAME.equalsIgnoreCase(name)) {
                addPropertyColumn(name, ReflectionUtils.findField(UserTO.class, name), prefcolumns);
            }
        }

        for (String name : prefMan.getList(getRequest(), Constants.PREF_USERS_PLAIN_ATTRS_VIEW)) {
            if (pSchemaNames.contains(name)) {
                prefcolumns.add(new AttrColumn<UserTO>(name, SchemaType.PLAIN));
            }
        }

        for (String name : prefMan.getList(getRequest(), Constants.PREF_USERS_DER_ATTRS_VIEW)) {
            if (dSchemaNames.contains(name)) {
                prefcolumns.add(new AttrColumn<UserTO>(name, SchemaType.DERIVED));
            }
        }

        // Add defaults in case of no selection
        if (prefcolumns.isEmpty()) {
            for (String name : UserDisplayAttributesModalPanel.DEFAULT_SELECTION) {
                addPropertyColumn(name, ReflectionUtils.findField(UserTO.class, name), prefcolumns);
            }

            prefMan.setList(getRequest(), getResponse(), Constants.PREF_USERS_DETAILS_VIEW,
                    Arrays.asList(UserDisplayAttributesModalPanel.DEFAULT_SELECTION));
        }

        columns.addAll(prefcolumns);
        return columns;
    }

    @Override
    public ActionsPanel<UserTO> getActions(final IModel<UserTO> model) {
        final ActionsPanel<UserTO> panel = super.getActions(model);

        panel.add(new ActionLink<UserTO>() {

            private static final long serialVersionUID = -7978723352517770644L;

            @Override
            public void onClick(final AjaxRequestTarget target, final UserTO ignore) {
                send(UserDirectoryPanel.this, Broadcast.EXACT,
                        new AjaxWizard.EditItemActionEvent<>(
                                new UserWrapper(new UserRestClient().read(model.getObject().getKey())),
                                target));
            }
        }, ActionType.EDIT, new StringBuilder().append(StandardEntitlement.USER_READ).append(",").
                append(StandardEntitlement.USER_UPDATE).toString()).setRealm(realm);

        panel.add(new ActionLink<UserTO>() {

            private static final long serialVersionUID = -7978723352517770644L;

            @Override
            public void onClick(final AjaxRequestTarget target, final UserTO ignore) {
                UserTO clone = SerializationUtils.clone(model.getObject());
                clone.setKey(null);
                clone.setUsername(model.getObject().getUsername() + "_clone");
                send(UserDirectoryPanel.this, Broadcast.EXACT,
                        new AjaxWizard.NewItemActionEvent<>(new UserWrapper(clone), target));
            }

            @Override
            protected boolean statusCondition(final UserTO modelObject) {
                return addAjaxLink.isVisibleInHierarchy() && realm.startsWith(SyncopeConstants.ROOT_REALM);
            }
        }, ActionType.CLONE, StandardEntitlement.USER_CREATE).setRealm(realm);

        panel.add(new ActionLink<UserTO>() {

            private static final long serialVersionUID = -7978723352517770644L;

            @Override
            public void onClick(final AjaxRequestTarget target, final UserTO ignore) {
                try {
                    UserRestClient.class.cast(restClient).mustChangePassword(
                            model.getObject().getETagValue(),
                            !model.getObject().isMustChangePassword(),
                            model.getObject().getKey());
                    SyncopeConsoleSession.get().info(getString(Constants.OPERATION_SUCCEEDED));
                    target.add(container);
                } catch (Exception e) {
                    LOG.error("While deleting object {}", model.getObject().getKey(), e);
                    SyncopeConsoleSession.get().error(StringUtils.isBlank(e.getMessage()) ? e.getClass().
                            getName() : e.getMessage());
                }
                ((BasePage) pageRef.getPage()).getNotificationPanel().refresh(target);
            }
        }, ActionType.MUSTCHANGEPASSWORD, StandardEntitlement.USER_UPDATE).setRealm(realm);

        if (wizardInModal) {
            panel.add(new ActionLink<UserTO>() {

                private static final long serialVersionUID = -4875218360625971340L;

                @Override
                public void onClick(final AjaxRequestTarget target, final UserTO ignore) {
                    IModel<AnyWrapper<UserTO>> formModel = new CompoundPropertyModel<>(
                            new AnyWrapper<>(model.getObject()));
                    displayAttributeModal.setFormModel(formModel);

                    target.add(displayAttributeModal.setContent(new ChangePasswordModal(
                            displayAttributeModal,
                            pageRef,
                            new UserWrapper(model.getObject()))));

                    displayAttributeModal.header(new Model<>(
                            getString("any.edit", new Model<>(new AnyWrapper<>(model.getObject())))));

                    displayAttributeModal.show(true);
                }
            }, ActionType.PASSWORD_RESET,
                    new StringBuilder().append(StandardEntitlement.USER_UPDATE).toString()).setRealm(realm);

            panel.add(new ActionLink<UserTO>() {

                private static final long serialVersionUID = -7978723352517770644L;

                @Override
                public void onClick(final AjaxRequestTarget target, final UserTO ignore) {
                    IModel<AnyWrapper<UserTO>> formModel = new CompoundPropertyModel<>(
                            new AnyWrapper<>(model.getObject()));
                    altDefaultModal.setFormModel(formModel);

                    target.add(altDefaultModal.setContent(new AnyStatusModal<>(
                            altDefaultModal,
                            pageRef,
                            formModel.getObject().getInnerObject(),
                            "resourceName",
                            true)));

                    altDefaultModal.header(new Model<>(
                            getString("any.edit", new Model<>(new AnyWrapper<>(model.getObject())))));

                    altDefaultModal.show(true);
                }
            }, ActionType.ENABLE, StandardEntitlement.USER_UPDATE).setRealm(realm);

            panel.add(new ActionLink<UserTO>() {

                private static final long serialVersionUID = -7978723352517770644L;

                @Override
                public void onClick(final AjaxRequestTarget target, final UserTO ignore) {
                    IModel<AnyWrapper<UserTO>> formModel = new CompoundPropertyModel<>(
                            new AnyWrapper<>(model.getObject()));
                    altDefaultModal.setFormModel(formModel);

                    target.add(altDefaultModal.setContent(new AnyStatusModal<>(
                            altDefaultModal,
                            pageRef,
                            formModel.getObject().getInnerObject(),
                            "resourceName",
                            false)));

                    altDefaultModal.header(new Model<>(
                            getString("any.edit", new Model<>(new AnyWrapper<>(model.getObject())))));

                    altDefaultModal.show(true);
                }
            }, ActionType.MANAGE_RESOURCES, StandardEntitlement.USER_UPDATE).setRealm(realm);

            panel.add(new ActionLink<UserTO>() {

                private static final long serialVersionUID = -7978723352517770644L;

                @Override
                public void onClick(final AjaxRequestTarget target, final UserTO ignore) {
                    target.add(utilityModal.setContent(new AnyPropagationTasks(
                            utilityModal, AnyTypeKind.USER, model.getObject().getKey(), pageRef)));

                    utilityModal.header(new StringResourceModel("any.propagation.tasks", model));
                    utilityModal.show(true);
                }
            }, ActionType.PROPAGATION_TASKS, StandardEntitlement.TASK_LIST);

            panel.add(new ActionLink<UserTO>() {

                private static final long serialVersionUID = -7978723352517770644L;

                @Override
                public void onClick(final AjaxRequestTarget target, final UserTO ignore) {
                    target.add(utilityModal.setContent(
                            new NotificationTasks(AnyTypeKind.USER, model.getObject().getKey(), pageRef)));
                    utilityModal.header(new StringResourceModel("any.notification.tasks", model));
                    utilityModal.show(true);
                    target.add(utilityModal);
                }
            }, ActionType.NOTIFICATION_TASKS, StandardEntitlement.TASK_LIST);
        }

        panel.add(new ActionLink<UserTO>() {

            private static final long serialVersionUID = -7978723352517770644L;

            @Override
            public void onClick(final AjaxRequestTarget target, final UserTO ignore) {
                try {
                    restClient.delete(model.getObject().getETagValue(), model.getObject().getKey());
                    SyncopeConsoleSession.get().info(getString(Constants.OPERATION_SUCCEEDED));
                    target.add(container);
                } catch (Exception e) {
                    LOG.error("While deleting object {}", model.getObject().getKey(), e);
                    SyncopeConsoleSession.get().error(StringUtils.isBlank(e.getMessage()) ? e.getClass().
                            getName() : e.getMessage());
                }
                ((BasePage) pageRef.getPage()).getNotificationPanel().refresh(target);
            }

            @Override
            protected boolean statusCondition(final UserTO modelObject) {
                return realm.startsWith(SyncopeConstants.ROOT_REALM);
            }
        }, ActionType.DELETE, StandardEntitlement.USER_DELETE, true).setRealm(realm);

        return panel;
    }

    public static class Builder extends AnyDirectoryPanel.Builder<UserTO, UserRestClient> {

        private static final long serialVersionUID = -6603152478702381900L;

        public Builder(final List<AnyTypeClassTO> anyTypeClassTOs, final String type, final PageReference pageRef) {
            super(anyTypeClassTOs, new UserRestClient(), type, pageRef);
            setShowResultPage(true);
        }

        @Override
        protected WizardMgtPanel<AnyWrapper<UserTO>> newInstance(final String id, final boolean wizardInModal) {
            return new UserDirectoryPanel(id, this, wizardInModal);
        }
    }
}
