/*
 * This file is part of a module with proprietary Enterprise Features.
 *
 * Licensed to Crate.io Inc. ("Crate.io") under one or more contributor
 * license agreements.  See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 *
 * To use this file, Crate.io must have given you permission to enable and
 * use such Enterprise Features and you must have a valid Enterprise or
 * Subscription Agreement with Crate.io.  If you enable or use the Enterprise
 * Features, you represent and warrant that you have a valid Enterprise or
 * Subscription Agreement with Crate.io.  Your use of the Enterprise Features
 * if governed by the terms and conditions of your Enterprise or Subscription
 * Agreement with Crate.io.
 */

package io.crate.operation.user;

import com.google.common.collect.ImmutableList;
import io.crate.exceptions.UserAlreadyExistsException;
import io.crate.metadata.UsersMetaData;
import io.crate.test.integration.CrateUnitTest;
import org.elasticsearch.cluster.metadata.MetaData;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.Is.is;

public class TransportUserActionTest extends CrateUnitTest {

    @Test
    public void testCreateFirstUser() throws Exception {
        UsersMetaData metaData = TransportCreateUserAction.putUser(null, "root");
        assertThat(metaData.users().size(), is(1));
        assertThat(metaData.users().get(0), is("root"));
    }

    @Test
    public void testCreateUserAlreadyExists() throws Exception {
        expectedException.expect(UserAlreadyExistsException.class);
        expectedException.expectMessage("User 'root' already exists");
        UsersMetaData oldMetaData = new UsersMetaData(ImmutableList.of("root"));
        TransportCreateUserAction.putUser(oldMetaData, "root");
    }

    @Test
    public void testCreateUser() throws Exception {
        UsersMetaData oldMetaData = new UsersMetaData(ImmutableList.of("Trillian"));
        UsersMetaData newMetaData = TransportCreateUserAction.putUser(oldMetaData, "Arthur");
        assertThat(newMetaData.users(), containsInAnyOrder("Trillian", "Arthur"));
    }

    @Test
    public void testDropUserNoUsersAtAll() throws Exception {
        assertThat(TransportDropUserAction.dropUser(MetaData.builder(), null, "root"), is(false));
    }

    @Test
    public void testDropNonExistingUser() throws Exception {
        boolean res = TransportDropUserAction.dropUser(
            MetaData.builder(),
            new UsersMetaData(ImmutableList.of("arthur")),
            "trillian"
        );
        assertThat(res, is(false));
    }

    @Test
    public void testDropUser() throws Exception {
        UsersMetaData oldMetaData = new UsersMetaData(ImmutableList.of("ford", "arthur"));
        MetaData.Builder mdBuilder = MetaData.builder();
        boolean res = TransportDropUserAction.dropUser(mdBuilder, oldMetaData, "arthur");
        assertThat(users(mdBuilder), contains("ford"));
        assertThat(res, is(true));
    }

    private static List<String> users(MetaData.Builder mdBuilder) {
        return ((UsersMetaData)mdBuilder.build().custom(UsersMetaData.TYPE)).users();
    }
}