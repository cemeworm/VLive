package com.hustunique.vlive.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.hustunique.vlive.R
import com.hustunique.vlive.databinding.FragmentFloatControlBinding
import com.hustunique.vlive.local.MemberInfo

/**
 *    author : Yuxuan Xiao
 *    e-mail : qpalwo@qq.com
 *    date   : 2021/5/25
 */
class FloatControlFragment : Fragment() {

    companion object {
        private const val TAG = "FloatControlFragment"
    }

    private val binding by lazy {
        FragmentFloatControlBinding.inflate(layoutInflater)
    }

    private val viewModel by viewModels<SceneViewModel>({ requireActivity() })

    private val listAdapter = UserListAdapter().apply {
        setOnItemClickListener { adapter, view, position ->
            viewModel.eventData.postValue(FlyEvent(data[position].modelObject?.getTransform()!!.first))
        }
    }

    private var userListShow = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding.rocker.onUpdate = { radians, progress ->
            viewModel.eventData.postValue(RockerEvent(radians, progress))
        }
        binding.modeSwitcher.setOnClickListener {
            val curState = binding.rocker.enable
            viewModel.eventData.postValue(ModeSwitchEvent(!curState))
            if (!curState) {
                binding.modeSwitcher.background =
                    requireActivity().getDrawable(R.drawable.round_btn_bg)
            } else {
                binding.modeSwitcher.background =
                    requireActivity().getDrawable(R.drawable.round_btn_bg_light)
            }
            binding.rocker.enable = !curState
        }
        binding.reset.setOnClickListener {
            viewModel.eventData.postValue(ResetEvent())
        }
        binding.userRecycler.apply {
            layoutManager = LinearLayoutManager(requireActivity())
            adapter = listAdapter
        }
        binding.userChoose.setOnClickListener {
            if (userListShow) {
                binding.userRecycler.visibility = View.GONE
                binding.grayLayer.visibility = View.GONE
            } else {
                binding.userRecycler.visibility = View.VISIBLE
                binding.grayLayer.visibility = View.VISIBLE
            }
            userListShow = !userListShow
        }
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.memberInfo.observe(viewLifecycleOwner) {
            listAdapter.setList(it)
        }
    }
}

class UserListAdapter : BaseQuickAdapter<MemberInfo, BaseViewHolder>(R.layout.item_user) {

    override fun convert(holder: BaseViewHolder, item: MemberInfo) {
        holder.setText(R.id.user_name, item.userName)
    }

}