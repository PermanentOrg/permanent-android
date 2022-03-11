package org.permanent.permanent.network.models

import org.permanent.permanent.models.ProfileItem

class ProfileItemsRequestContainer(csrf: String?) {
    private var RequestVO: ProfileItemsRequestVO = ProfileItemsRequestVO()

    init {
        RequestVO.csrf = csrf
        val dataList = (RequestVO.data as ArrayList)
        dataList.add(Data())
        RequestVO.data = dataList
    }

    fun addProfileItems(profileItems: List<ProfileItem>): ProfileItemsRequestContainer {
        for ((index, profileItem) in profileItems.withIndex()) {
            val profileItemVO = SimpleProfileItemVO(profileItem)
            if (index == 0) RequestVO.data?.get(0)?.Profile_itemVO = profileItemVO
            else {
                val newData = Data()
                newData.Profile_itemVO = profileItemVO
                (RequestVO.data as ArrayList).add(newData)
            }
        }
        return this
    }
}