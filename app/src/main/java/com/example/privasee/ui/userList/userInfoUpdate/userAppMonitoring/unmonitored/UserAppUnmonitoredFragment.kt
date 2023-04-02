package com.example.privasee.ui.userList.userInfoUpdate.userAppMonitoring.unmonitored

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.privasee.AppAccessService
import com.example.privasee.R
import com.example.privasee.database.viewmodel.AppViewModel
import com.example.privasee.database.viewmodel.RestrictionViewModel
import com.example.privasee.databinding.FragmentUserAppUnmonitoredBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class UserAppUnmonitoredFragment : Fragment() {

    private var _binding: FragmentUserAppUnmonitoredBinding? = null
    private val binding get() = _binding!!

    private lateinit var mRestrictionViewModel: RestrictionViewModel
    private lateinit var mAppViewModel: AppViewModel

    private var job1: Job? = null
    private var job2: Job? = null
    private var job3: Job? = null

    private val args: UserAppUnmonitoredFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserAppUnmonitoredBinding.inflate(inflater, container, false)

        // Recyclerview adapter
        val adapter = UserAppUnmonitoredAdapter()
        binding.rvAppUnmonitored.adapter = adapter
        binding.rvAppUnmonitored.layoutManager = LinearLayoutManager(requireContext())

        // Nav Arguments
        val userId = args.userId
        val bundle = Bundle()
        bundle.putInt("userId", userId)

        // Database queries
        mRestrictionViewModel = ViewModelProvider(this)[RestrictionViewModel::class.java]
        mAppViewModel = ViewModelProvider(this)[AppViewModel::class.java]

        // Observe Live data of unmonitored list
        job1 = lifecycleScope.launch {
            val unmonitoredList = mRestrictionViewModel.getAllUnmonitoredApps(userId)
            withContext(Dispatchers.Main) {
                unmonitoredList.observe(viewLifecycleOwner) {
                    adapter.setData(it)
                }
            }
        }

        // Buttons
        binding.btnMonitoredList.setOnClickListener {
            findNavController().navigate(R.id.action_appUnmonitoredFragment_to_appMonitoredFragment, bundle)
        }

        // Update new list of monitored apps in database
        binding.btnApplyUnmonitored.setOnClickListener {
            val newRestriction = adapter.getCheckedApps()
            job2 = lifecycleScope.launch(Dispatchers.IO) {
                for (restrictionId in newRestriction)
                    mRestrictionViewModel.updateMonitoredApps(restrictionId, true)
            }

            if (newRestriction.isNotEmpty()) {
                // Send data to Accessibility Service on monitoring
                job3 = lifecycleScope.launch(Dispatchers.IO) {
                    val newMonitoredListPackageName: MutableList<String> = mutableListOf()
                    for (restrictionId in newRestriction) {
                        val appId = mRestrictionViewModel.getPackageId(restrictionId)
                        newMonitoredListPackageName.add(mAppViewModel.getPackageName(appId))
                    }
                    val intent = Intent(requireContext(), AppAccessService::class.java)
                    intent.putExtra("action", "addMonitor" )
                    intent.putStringArrayListExtra("addMonitoredAppPackageName", ArrayList(newMonitoredListPackageName))
                    requireContext().startService(intent)
                }

                findNavController().navigate(
                    R.id.action_appUnmonitoredFragment_to_appMonitoredFragment,
                    bundle
                )
            }

        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        job1?.cancel()
        job2?.cancel()
        job3?.cancel()
        _binding = null
    }

}