/*
 * Copyright (c) 2021 Carlos 'Klozz' jesus <klozz@thexperienceproject.org>
 * Copyright (c) 2021 The XPerience Project
 *
 *       This program is free software: you can redistribute it and/or modify
 *       it under the terms of the GNU General Public License as published by
 *       the Free Software Foundation, either version 2 of the License, or
 *       (at your option) any later version.
 *
 *       This program is distributed in the hope that it will be useful,
 *       but WITHOUT ANY WARRANTY; without even the implied warranty of
 *       MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *       GNU General Public License for more details.
 *
 *       You should have received a copy of the GNU General Public License
 *       along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package mx.xperience.xperiencewallpapers

import android.app.ProgressDialog
import android.app.WallpaperManager
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListAdapter
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.SimpleOnPageChangeListener
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import java.io.IOException
import java.util.*

@Suppress("DEPRECATION")
class WallpaperActivity : FragmentActivity() {
    /**
     * The [androidx.viewpager.widget.PagerAdapter] that will provide fragments for each of the
     * sections. We use a [androidx.fragment.app.FragmentPagerAdapter] derivative, which will
     * keep every loaded fragment in memory. If this becomes too memory intensive, it may be best
     * to switch to a [androidx.fragment.app.FragmentStatePagerAdapter].
     */
    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null

    /**
     * The [ViewPager] that will host the section contents.
     */
    private var mViewPager: ViewPager? = null

    /**
     * The [TabLayout] that will host the page indicators.
     */
    private var mTabLayout: TabLayout? = null

    /**
     * The [Snackbar] that is responsible for showing the info text.
     */
    private var mSnackbar: Snackbar? = null
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_main)

        // Create the adapter that will return a fragment for each of the three primary sections
        // of the app.
        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById<View>(R.id.pager) as ViewPager
        mViewPager!!.adapter = mSectionsPagerAdapter
        mViewPager!!.setOnPageChangeListener(object : SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                sCurrentPosition = position
            }
        })
        mTabLayout = findViewById<View>(R.id.page_indicator) as TabLayout
        mTabLayout!!.setupWithViewPager(mViewPager, true)
        sIcons.clear()
        sIcons.add(R.drawable.ic_home)
        sIcons.add(R.drawable.ic_lockscreen)
        sIcons.add(R.drawable.ic_both)
        sWallpaperManager = WallpaperManager.getInstance(this)
        sWallpapers.clear()
        fetchWallpapers(R.array.wallpapers)
        mSnackbar = Snackbar.make(mViewPager!!, R.string.longpress_info, 4000 /* 4 seconds */)
        mSnackbar!!.show()
    }

    public override fun onResume() {
        super.onResume()
        mSnackbar!!.show()
    }

    /**
     * A [FragmentPagerAdapter] that returns a fragment corresponding to one of the primary
     * sections of the app.
     */
    private inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
        override fun getItem(i: Int): Fragment {
            val fragment: Fragment = WallpaperFragment()
            val args = Bundle()
            args.putInt(WallpaperFragment.ARG_SECTION_NUMBER, i)
            fragment.arguments = args
            return fragment
        }

        override fun getCount(): Int {
            return sWallpapers.size
        }
    }

    class WallpaperFragment : Fragment() {
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                                  savedInstanceState: Bundle?): View? {
            val args = arguments
            val context = context
            val loader = WallpaperLoader(activity)
            val adapter: ListAdapter = object : ArrayAdapter<Int?>(context!!,
                    android.R.layout.select_dialog_item,
                    android.R.id.text1, sIcons as List<Int?>) {
                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val items = context!!.resources.getStringArray(
                            R.array.wallpaper_options)
                    val padding = context.resources.getDimensionPixelSize(
                            R.dimen.wallpaper_text_padding).toFloat()
                    val leftPadding = context.resources.getDimensionPixelSize(
                            R.dimen.wallpaper_text_padding_left).toFloat()
                    val textSize = context.resources.getDimensionPixelSize(
                            R.dimen.wallpaper_text_size).toFloat()
                    val view = super.getView(position, convertView, parent)
                    val text = view.findViewById<View>(android.R.id.text1) as TextView
                    text.text = items[position]
                    text.textSize = textSize
                    text.setCompoundDrawablesWithIntrinsicBounds(sIcons[position], 0, 0, 0)
                    text.setPadding(leftPadding.toInt(), 0, 0, 0)
                    text.compoundDrawablePadding = padding.toInt()
                    return view
                }
            }
            val imageView = ImageView(context)
            imageView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            imageView.setImageResource(sWallpapers[args!!.getInt(ARG_SECTION_NUMBER)])
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            imageView.setOnLongClickListener {
                val bitmap = BitmapFactory.decodeResource(resources,
                        sWallpapers[sCurrentPosition])
                AlertDialog.Builder(context)
                        .setTitle(R.string.wallpaper_instructions)
                        .setAdapter(adapter) { dialog, selectedItemIndex ->
                            val whichWallpaper: Int
                            whichWallpaper = if (selectedItemIndex == 0) {
                                WallpaperManager.FLAG_SYSTEM
                            } else if (selectedItemIndex == 1) {
                                WallpaperManager.FLAG_LOCK
                            } else {
                                (WallpaperManager.FLAG_SYSTEM
                                        or WallpaperManager.FLAG_LOCK)
                            }
                            loader.setBitmap(bitmap)
                            loader.setType(whichWallpaper)
                            loader.execute()
                        }
                        .show()
                true
            }
            return imageView
        }

        companion object {
            const val ARG_SECTION_NUMBER = "section_number"
        }
    }

    private fun fetchWallpapers(list: Int) {
        val walls = resources.getStringArray(list)
        for (wall in walls) {
            val res = resources.getIdentifier(wall, "drawable", packageName)
            if (res != 0) {
                sWallpapers.add(res)
                mSectionsPagerAdapter!!.notifyDataSetChanged()
            }
        }
    }

    private class WallpaperLoader(var mActivity: FragmentActivity?) : AsyncTask<Int?, Void?, Boolean>() {
        var mWallpaperType = 0
        var mBitmap: Bitmap? = null
        var mDialog: ProgressDialog? = null
        fun setBitmap(bitmap: Bitmap) {
            mBitmap = bitmap
        }

        fun setType(type: Int) {
            mWallpaperType = type
        }

        override fun onPostExecute(success: Boolean) {
            mDialog!!.dismiss()
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_HOME)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            mActivity!!.startActivity(intent)
            mActivity!!.finish()
        }

        override fun onPreExecute() {
            mDialog = ProgressDialog.show(mActivity, null, mActivity!!.getString(R.string.applying))
        }

        override fun doInBackground(vararg params: Int?): Boolean {
            return try {
                sWallpaperManager!!.setBitmap(mBitmap, null, true, mWallpaperType)

                // Help GC
                mBitmap!!.recycle()
                true
            } catch (e: IOException) {
                Log.e(TAG, "Exception ocurred while trying to set wallpaper", e)
                false
            } catch (e: OutOfMemoryError) {
                Log.e(TAG, "Exception ocurred while trying to set wallpaper", e)
                false
            }
        }

    }

    companion object {
        private val TAG = WallpaperActivity::class.java.simpleName

        /**
         * The [WallpaperManager] used to set wallpaper.
         */
        private var sWallpaperManager: WallpaperManager? = null

        /**
         * The [Integer] that stores current viewpager position
         */
        private var sCurrentPosition = 0

        /**
         * The [ArrayList] that will host the wallpapers resource ID's.
         */
        private val sWallpapers = ArrayList<Int>()

        /**
         * The [ArrayList] that holds the current wallpaper modes icon.
         */
        private val sIcons = ArrayList<Int>()
    }
}