package com.imma.service

object RouteConstants {
    const val LOGIN: String = "/login/access-token"

    const val USER_SAVE: String = "/user/save"
    const val USER_FIND_BY_ID: String = "/user/find/by-id"
    const val USER_LIST_BY_NAME: String = "/user/list/by-name"
    const val USER_LIST_BY_NAME_FOR_HOLDER: String = "/user/list/by-name/for-holder"
    const val USER_LIST_BY_IDS_FOR_HOLDER: String = "/user/list/by-ids/for-holder"

    const val USER_GROUP_SAVE: String = "/user-group/save"
    const val USER_GROUP_FIND_BY_ID: String = "/user-group/find/by-id"
    const val USER_GROUP_LIST_BY_NAME: String = "/user-group/list/by-name"
    const val USER_GROUP_LIST_BY_NAME_FOR_HOLDER: String = "/user-group/list/by-name/for-holder"
    const val USER_GROUP_LIST_BY_IDS_FOR_HOLDER: String = "/user-group/list/by-ids/for-holder"

    const val SPACE_SAVE: String = "/space/save"
    const val SPACE_FIND_BY_ID: String = "/space/find/by-id"
    const val SPACE_LIST_BY_NAME: String = "/space/list/by-name"
    const val SPACE_LIST_BY_NAME_FOR_HOLDER: String = "/space/list/by-name/for-holder"
    const val SPACE_LIST_BY_IDS_FOR_HOLDER: String = "/space/list/by-ids/for-holder"

    const val CONNECT_SPACE: String = "/space/connect"
    const val CONNECTED_SPACE_RENAME: String = "/connected-space/rename"
    const val CONNECTED_SPACE_DELETE: String = "/connected-space/delete"
    const val CONNECTED_SPACE_LIST_BY_MINE: String = "/connected-space/list/mine"

    const val CONNECTED_SPACE_GRAPHICS_SAVE: String = "/connected-space/graphics/save"
    const val CONNECTED_SPACE_GRAPHICS_LIST_BY_MINE: String = "/connected-space/graphics/list/mine"
}