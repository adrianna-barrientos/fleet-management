package co.tcc.koga.android.ui.chat

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import co.tcc.koga.android.R
import co.tcc.koga.android.data.database.entity.UserEntity
import co.tcc.koga.android.databinding.ChatFragmentBinding
import co.tcc.koga.android.ui.MainActivity
import co.tcc.koga.android.utils.*
import kotlinx.android.synthetic.main.chat_fragment.*
import java.io.*
import javax.inject.Inject


class ChatFragment : Fragment(R.layout.chat_fragment) {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var binding: ChatFragmentBinding
    private val args: ChatFragmentArgs by navArgs()
    private val viewModel by viewModels<ChatViewModel> { viewModelFactory }

    private lateinit var adapter: MessageAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity() as MainActivity).mainComponent.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ChatFragmentBinding.inflate(layoutInflater)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupViews()
        setupRecyclerView()
        viewModelObservers()
    }

    private fun viewModelObservers() {
        viewModel.run {
            chatId = args.chat.id
            openChat()
            getMessages()
            observeMessageUpdates()
            observeChatUpdates(args.chat)
            observeUserUpdates()

            messages.observe(viewLifecycleOwner) { messages ->
                println("MESSAGES")
                adapter.submitList(messages)
                binding.recyclerViewChatMessages.scrollToPosition(messages.size - 1)
            }

            chat.observe(viewLifecycleOwner) { chat ->
                binding.run {
                    loadChatAvatar(imageViewChatAvatar)
                    if (chat.user !== null) {
                        textViewChatTitle.text = chat.user?.name
                        imageViewUserStatusOnline.hide()
                        imageViewUserStatusOffline.hide()
                        if (chat.user?.status == "ONLINE") {
                            imageViewUserStatusOnline.show()
                        } else {
                            imageViewUserStatusOffline.show()
                        }
                    } else {
                        textViewChatTitle.text = chat.groupName
                    }
                }

            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        hideKeyboard()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.chat_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.nav_speaker -> {
                linear_layout_chat_speaker.show()
                linear_layout_chat_keyboard.hide()
                item.isVisible = false
                toolbar_chat.menu.findItem(R.id.nav_keyboard).isVisible = true
                true
            }
            R.id.nav_keyboard -> {
                linear_layout_chat_speaker.hide()
                linear_layout_chat_keyboard.show()
                item.isVisible = false
                toolbar_chat.menu.findItem(R.id.nav_speaker).isVisible = true
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    private fun setupRecyclerView() {
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.stackFromEnd = true
        adapter = MessageAdapter(viewModel.username, args.chat.members)
        binding.recyclerViewChatMessages.layoutManager = linearLayoutManager
        binding.recyclerViewChatMessages.adapter = adapter
        binding.recyclerViewChatMessages.viewTreeObserver.addOnGlobalLayoutListener { scrollToEnd() }
    }

    private fun scrollToEnd() =
        (adapter.itemCount - 1).takeIf { it > 0 }
            ?.let(binding.recyclerViewChatMessages::smoothScrollToPosition)

    private fun setupToolbar() {
        text_view_chat_title.text =
            if (args.chat.user !== null) args.chat.user?.name else args.chat.groupName
        loadChatAvatar(image_view_chat_avatar)
        toolbar_chat.apply {
            inflateMenu(R.menu.chat_menu)
            setOnMenuItemClickListener { item ->
                onOptionsItemSelected(item)
                true
            }
            setNavigationOnClickListener {
                if (findNavController().popBackStack(R.id.chatsFragment, false)) {

                } else {
                    findNavController().navigate(R.id.chatsFragment)
                }
//                findNavController().popBackStack()
            }
            setOnClickListener {
                openChatDetails()
            }
        }
    }

    private fun loadChatAvatar(imageView: ImageView) {
        val url: String? = if (args.chat.user !== null) {
            if (args.chat.user?.avatarUrl != "") args.chat.user?.avatarUrl as String else getUserAvatar(
                args.chat.user as UserEntity
            )
        } else {
            args.chat.avatarUrl
        }

        Avatar.loadImage(
            requireContext(),
            imageView,
            url,
            if (args.chat.user !== null) R.drawable.ic_round_person else R.drawable.ic_round_group
        )
    }

    private fun hideKeyboard() {
        val parentActivity = requireActivity()
        if (parentActivity is AppCompatActivity) {
            parentActivity.hideKeyboard()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupViews() {
        edit_text_message.addTextChangedListener {
            recycler_view_chat_messages.scrollToPosition(adapter.itemCount - 1)
            if (it != null) {
                if (it.isNotEmpty()) {
                    image_button_send_message.show()
                } else {
                    image_button_send_message.hide()
                }
            }
        }


        image_button_send_audio.setOnLongClickListener {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                val permissions = arrayOf(
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
                requestPermissions(permissions, 0)
            } else {
                viewModel.startRecording()
            }
            true
        }

        image_button_send_audio.setOnClickListener {

        }

        image_button_send_audio.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_UP -> {
                    viewModel.stopRecording()

                    return@setOnTouchListener true
                }
            }
            false
        }


        image_button_send_message.setOnClickListener {
            val message = edit_text_message.text.toString()
            if (message.isNotEmpty()) {
                edit_text_message.text.clear()
                viewModel.sendMessage(
                    message,
                    args.chat.id
                )
            }
        }
    }

    private fun openChatDetails() {
//        if (args.chat.isPrivate) {
//
//        } else {
//            val contactDetailsView =
//                GroupDetailsFragmentBinding.inflate(LayoutInflater.from(requireContext()))
//            contactDetailsView.recyclerViewGroupMembers.layoutManager = LinearLayoutManager(context)
//            contactDetailsView.recyclerViewGroupMembers.adapter =
//                UserAdapter(requireContext(), args.chat.members!!, fun(_) {})
//            MaterialAlertDialogBuilder(context as Context)
//                .setView(contactDetailsView.root)
//                .show()
//        }


    }
}

