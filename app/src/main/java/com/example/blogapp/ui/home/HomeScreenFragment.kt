package com.example.blogapp.ui.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.blogapp.R
import com.example.blogapp.core.Result
import com.example.blogapp.core.hide
import com.example.blogapp.core.show
import com.example.blogapp.data.model.Post
import com.example.blogapp.data.remote.home.HomeScreenDataSource
import com.example.blogapp.databinding.FragmentHomeScreenBinding
import com.example.blogapp.domain.home.HomeScreenRepoImpl
import com.example.blogapp.presentation.HomeScreenViewModel
import com.example.blogapp.presentation.HomeScreenViewModelFactory
import com.example.blogapp.ui.home.adapter.HomeScreenAdapter
import com.example.blogapp.ui.home.adapter.OnPostClickListener
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class HomeScreenFragment : Fragment(R.layout.fragment_home_screen), OnPostClickListener {

    private lateinit var binding: FragmentHomeScreenBinding
    private val viewModel by viewModels<HomeScreenViewModel> {
        HomeScreenViewModelFactory(
            HomeScreenRepoImpl(
                HomeScreenDataSource()
            )
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeScreenBinding.bind(view)

        var self = this

        viewLifecycleOwner.lifecycleScope.launch {
            // Inicia la app queremos que la info se empiece a observar/colectar cuando se crea el fragment, cuando se detiene el fragment no recibe mas info, cuando volvimos a abrir la app vuelve a STARTED.
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.latestPosts.collect{ result ->
                    when (result) {
                        is Result.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                        }

                        is Result.Success -> {
                            binding.progressBar.visibility = View.GONE
                            if(result.data.isEmpty()){
                                binding.postsEmpty.show()
                                return@collect
                            }else{
                                binding.postsEmpty.hide()
                            }
                            binding.rvHome.adapter = HomeScreenAdapter(result.data, self)
                        }

                        is Result.Failure -> {
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(
                                requireContext(),
                                "Ocurrió un error: ${result.exception}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }
        /*
        viewModel.fetchLatestPosts().observe(viewLifecycleOwner, Observer { result ->
            when (result) {
                is Result.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }

                is Result.Success -> {
                    binding.progressBar.visibility = View.GONE
                    if(result.data.isEmpty()){
                        binding.postsEmpty.show()
                        return@Observer
                    }else{
                        binding.postsEmpty.hide()
                    }
                    binding.rvHome.adapter = HomeScreenAdapter(result.data, this)
                }

                is Result.Failure -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(
                        requireContext(),
                        "Ocurrió un error: ${result.exception}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        })
        */

    }

    override fun onLikeButtonClick(post: Post, liked: Boolean) {
        viewModel.registerLikeButtonState(post.id, liked).observe(viewLifecycleOwner){ result ->
            when (result) {
                is Result.Loading -> {

                }

                is Result.Success -> {

                }

                is Result.Failure -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(
                        requireContext(),
                        "Ocurrió un error: ${result.exception}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}