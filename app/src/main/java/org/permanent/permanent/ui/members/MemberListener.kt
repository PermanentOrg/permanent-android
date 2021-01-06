package org.permanent.permanent.ui.members

import org.permanent.permanent.models.Account

interface MemberListener {
    fun onMemberEdit(member: Account)
}