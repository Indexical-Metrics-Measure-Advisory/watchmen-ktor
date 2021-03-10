package com.imma.service

object RouteConstants {
    const val LOGIN: String = "/login/access-token"

    const val USER_SAVE: String = "/user"
    const val USER_FIND_BY_ID: String = "/user"
    const val USER_LIST_BY_NAME: String = "/user/name"
    const val USER_LIST_BY_NAME_FOR_HOLDER: String = "/user/name/holder"
    const val USER_LIST_BY_IDS_FOR_HOLDER: String = "/user/ids"

    const val USER_GROUP_SAVE: String = "/user_group"
    const val USER_GROUP_FIND_BY_ID: String = "/user_group"
    const val USER_GROUP_LIST_BY_NAME: String = "/user_group/name"
    const val USER_GROUP_LIST_BY_NAME_FOR_HOLDER: String = "/user_group/name/holder"
    const val USER_GROUP_LIST_BY_IDS_FOR_HOLDER: String = "/user_groups/ids"

    const val SPACE_SAVE: String = "/space"
    const val SPACE_FIND_BY_ID: String = "/space"
    const val SPACE_LIST_BY_NAME: String = "/space/name"
    const val SPACE_LIST_BY_NAME_FOR_HOLDER: String = "/space/name/holder"
    const val SPACE_LIST_BY_IDS_FOR_HOLDER: String = "/space/ids"
}