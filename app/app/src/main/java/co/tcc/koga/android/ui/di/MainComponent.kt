package co.tcc.koga.android.ui.di

import co.tcc.koga.android.MainActivity
import co.tcc.koga.android.ui.chat.ChatFragment
import co.tcc.koga.android.ui.chats.ChatsFragment
import co.tcc.koga.android.ui.new_group.NewGroupFragment
import co.tcc.koga.android.ui.auth.login.LoginFragment
import co.tcc.koga.android.ui.new_chat.NewChatFragment
import co.tcc.koga.android.ui.settings.SettingsFragment
import co.tcc.koga.android.ui.splash_screen.SplashScreenFragment
import dagger.Subcomponent


@Subcomponent(modules = [MainModule::class])
interface MainComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): MainComponent
    }

    fun inject(activity: MainActivity)
    fun inject(fragment: LoginFragment)
    fun inject(fragment: ChatsFragment)
    fun inject(fragment: ChatFragment)
    fun inject(fragment: SettingsFragment)
    fun inject(fragment: NewChatFragment)
    fun inject(fragment: NewGroupFragment)
    fun inject(fragment: SplashScreenFragment)
}