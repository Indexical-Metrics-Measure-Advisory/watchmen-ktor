package com.imma.rest

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
    const val AVAILABLE_SPACE_LIST_MINE: String = "/space/list/available/mine"

    const val TOPIC_SAVE: String = "/topic/save"
    const val TOPIC_FIND_BY_ID: String = "/topic/find/by-id"
    const val TOPIC_LIST_BY_NAME: String = "/topic/list/by-name"
    const val TOPIC_LIST_BY_NAME_FOR_HOLDER: String = "/topic/list/by-name/for-holder"
    const val TOPIC_LIST_BY_IDS_FOR_HOLDER: String = "/topic/list/by-ids/for-holder"

    const val CONNECT_SPACE_BY_ME: String = "/space/connect/me"
    const val CONNECTED_SPACE_RENAME_BY_ME: String = "/connected-space/rename/me"
    const val CONNECTED_SPACE_DELETE_BY_ME: String = "/connected-space/delete/me"
    const val CONNECTED_SPACE_LIST_MINE: String = "/connected-space/list/mine"

    const val CONNECTED_SPACE_GRAPHICS_SAVE_BY_ME: String = "/connected-space/graphics/save/me"
    const val CONNECTED_SPACE_GRAPHICS_LIST_MINE: String = "/connected-space/graphics/list/mine"

    const val DASHBOARD_SAVE_BY_ME: String = "/dashboard/me"
    const val DASHBOARD_RENAME_BY_ME: String = "/dashboard/rename/me"
    const val DASHBOARD_DELETE_BY_ME: String = "/dashboard/delete/me"
    const val DASHBOARD_LIST_MINE: String = "/dashboard/list/mine"

    const val SUBJECT_SAVE_BY_ME: String = "/subject/me"
    const val SUBJECT_RENAME_BY_ME: String = "/subject/rename/me"
    const val SUBJECT_DELETE_BY_ME: String = "/subject/delete/me"

    const val REPORT_SAVE_BY_ME: String = "/report/me"
    const val REPORT_RENAME_BY_ME: String = "/report/rename/me"
    const val REPORT_DELETE_BY_ME: String = "/report/delete/me"
    const val REPORT_LIST_BY_NAME: String = "/report/list/by-name"

    const val FAVORITE_SAVE: String = "/favorite/save"
    const val FAVORITE: String = "/favorite"
}