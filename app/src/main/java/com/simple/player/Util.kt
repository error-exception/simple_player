package com.simple.player

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.res.Resources
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.net.wifi.WifiManager
import android.os.Build
import android.util.DisplayMetrics
import android.util.TypedValue
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.customview.widget.ViewDragHelper
import androidx.drawerlayout.widget.DrawerLayout
import com.simple.player.service.SimpleService
import com.simple.player.util.BitmapUtil
import com.simple.player.drawable.RoundDrawable
import java.lang.Exception
import java.lang.StringBuilder
import java.lang.reflect.Field
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.HashMap
import kotlin.math.abs
import kotlin.math.max

@SuppressLint("StaticFieldLeak")
object Util {
    lateinit var mContext: Context
    var width = 0
    var height = 0

    fun dpToPx(dp: Float): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
            mContext.resources.displayMetrics).toInt()
    }

    fun setContext(context: Context) {
        mContext = context
        mListAlbumWidth =
            context.resources.getDimension(R.dimen.list_album_width).toInt()
    }

    fun timeString(milliseconds: Int): String {
        var temp = milliseconds
        temp /= 1000
        val sb = StringBuilder()
        return if (temp % 60 < 10) {
            sb.append(0).append(temp / 60).append(':').append(0).append(temp % 60).toString()
        } else {
            sb.append(0).append(temp / 60).append(':').append(temp % 60).toString()
        }
    }

    private var mArtworkCache: HashMap<Int, Drawable>? = HashMap()

    //    private static class FastBitmapDrawable extends Drawable{
    //        private Bitmap mBit;
    //        public FastBitmapDrawable(Bitmap bit){
    //            mBit = bit;
    //        }
    //        @Override
    //        public void draw(Canvas p1){
    //            p1.drawBitmap(mBit,0,0,null);
    //        }
    //        @Override
    //        public void setAlpha(int p1){}
    //        @Override
    //        public void setColorFilter(ColorFilter p1){}
    //        @Override
    //        public int getOpacity(){
    //            return 0;
    //        }
    //        public Bitmap getBitmap() {
    //            return mBit;
    //        }
    //    }
    private var mDefaultArtwork: RoundDrawable? = null
    private var mListAlbumWidth = 0
    fun defaultArtwork(): Drawable? {
        if (mDefaultArtwork != null) {
            return mDefaultArtwork
        }
        val m = BitmapUtil.getBitmapQuick(R.drawable.default_artwork, 64, 64)
        mDefaultArtwork = RoundDrawable(m!!)
        return mDefaultArtwork
    }

    //    public static Drawable getCacheArtwork(int id){
    //        Drawable d = null;
    //        if (mArtworkCache == null) {
    //            mArtworkCache = new HashMap<>();
    //        }
    //        d = mArtworkCache.get(id);
    //        if (d != null) {
    //            return d;
    //        }
    //        byte[] src = getArtworkBytes(id);
    //        if (src == null || src.length == 0) {
    //            d = defaultArtwork();
    //            mArtworkCache.put(id,d);
    //            return d;
    //        }
    //        Bitmap b = getBitmapQuick(src,Util.dpToPx(48),Util.dpToPx(48));
    //        if (b != null) {
    //            d = new RoundDrawable(b);
    //        } else {
    //            d = defaultArtwork();
    //        }
    //        mArtworkCache.put(id,d);
    //        return d;
    //    }
    //    public static void refreshArtworkCache(int id) {
    //        byte[] src = getArtworkBytes(id);
    //        if (src != null) {
    //            Bitmap a = getBitmapQuick(src, mListAlbumWidth, mListAlbumWidth);
    //            RoundDrawable f = (RoundDrawable)mArtworkCache.get(id);
    //            f.getBitmap().recycle();
    //            f = null;
    //            f = new RoundDrawable(a);
    //            mArtworkCache.put(id, f);
    //        }
    //    }
    //    public static void deleteCacheArtwork(int id) {
    //        RoundDrawable tmp = ((RoundDrawable)mArtworkCache.get(id));
    //        if (tmp != null && tmp != mDefaultArtwork) {
    //            tmp.getBitmap().recycle();
    //        }
    //        mArtworkCache.remove(id);
    //    }
    //
    //    public static boolean hasArtworkCache(int id) {
    //        return mArtworkCache != null && mArtworkCache.get(id) != null;
    //    }
    //
    fun bindService(context: Context, cnt: ServiceConnection?) {
        val intent = Intent()
        intent.setPackage(context.packageName)
        intent.action = SimpleService.ACTION_SIMPLE_SERVICE
        context.bindService(intent, cnt!!, Service.BIND_AUTO_CREATE)
    }

    fun toast(t: Any) {
        Toast.makeText(mContext, t.toString(), Toast.LENGTH_LONG).show()
    }

    private val mProgressDialogMap = HashMap<Int, ProgressDialog>()

    /*
    *    ????????????????????????????????????
    *    int id  ????????????????????????????????????????????????int??????????????????????????????
    *    String msg  ??????????????????????????????
    */
    fun showProgressDialog(context: Context?, id: Int, msg: String?) {
        val dialog = ProgressDialog(context)
        dialog.setMessage(msg)
        dialog.setCancelable(false)
        dialog.show()
        mProgressDialogMap[id] = dialog
    }

    fun closeProgressDialog(id: Int) {
        mProgressDialogMap[id]!!.dismiss()
        mProgressDialogMap.remove(id)
    }

    fun release() {
        mArtworkCache!!.clear()
        mArtworkCache = null
        mDefaultArtwork = null
    }

    fun getRatio(a: Int, b: Int): IntArray {
        val result = intArrayOf(a, b)
        val primeNumbers = intArrayOf(2, 3, 5, 7, 11, 13, 17, 19, 23)
        var isValid = false
        label@ do {
            for (g in primeNumbers.indices) {
                val t = result[0] % primeNumbers[g]
                val p = result[1] % primeNumbers[g]
                if (t == 0 && p == 0) {
                    result[0] /= primeNumbers[g]
                    result[1] /= primeNumbers[g]
                    isValid = true
                    continue@label
                }
            }
            isValid = false
        } while (isValid)
        return result
    }

    private fun int2Ip(ip: Int): String {
        val s = StringBuilder()
        val a = ip and 0xFF
        val b = ip shr 8 and 0xFF
        val c = ip shr 16 and 0xFF
        val d = ip shr 24 and 0xFF
        s.append(a).append('.').append(b).append('.')
            .append(c).append('.').append(d)
        return s.toString()
    }

    fun getInetAddress(): String? {
        val manager = mContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (manager.isWifiEnabled) {
            val info = manager.connectionInfo
            val ipAddress = info.ipAddress
            return int2Ip(ipAddress)
        }
        try {
            val networkInterfaces = NetworkInterface.getNetworkInterfaces()
            while (networkInterfaces.hasMoreElements()) {
                val networkInterface = networkInterfaces.nextElement()
                val addresses = networkInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val inetAddress = addresses.nextElement()
                    if (!inetAddress.isLoopbackAddress && inetAddress is Inet4Address) {
                        return inetAddress.getHostAddress()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    var Int.dps: Int
        get() {
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(),
                Resources.getSystem().displayMetrics).toInt()
        }
        private set(value) {}

    fun AppCompatActivity.getScreenRect(): Rect {
        val rect = Rect()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            val metric = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(metric)
            rect.set(0, 0, metric.widthPixels, metric.heightPixels)
        } else {
            val bounds = windowManager.currentWindowMetrics.bounds
            rect.set(0, 0, bounds.width(), bounds.height())
        }
        return rect
    }

    fun setCustomLeftEdgeSize(activity: AppCompatActivity, drawerLayout: DrawerLayout, displayWidthPercentage: Float) {
        try {
            // find ViewDragHelper and set it accessible
            val leftDraggerField = drawerLayout.javaClass.getDeclaredField("mRightDragger")
                ?: return
            leftDraggerField.isAccessible = true
            val leftDragger = leftDraggerField.get(drawerLayout) as ViewDragHelper
            // find edgesize and set is accessible
            val edgeSizeField: Field = leftDragger.javaClass.getDeclaredField("mEdgeSize")
            edgeSizeField.isAccessible = true
            val edgeSize: Int = edgeSizeField.getInt(leftDragger)
            // set new edgesize
            val screenRect = activity.getScreenRect()
            edgeSizeField.setInt(
                leftDragger,
                max(edgeSize, (screenRect.width() * displayWidthPercentage).toInt())
            )

            val leftCallbackField = drawerLayout.javaClass.getDeclaredField("mRightCallback")
            leftCallbackField.isAccessible = true
            val leftCallback = leftCallbackField.get(drawerLayout) as ViewDragHelper.Callback
            val peekRunnableField = leftCallback.javaClass.getDeclaredField("mPeekRunnable")
            peekRunnableField.isAccessible = true
            peekRunnableField.set(leftCallback, Runnable {  })


        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: java.lang.IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
    }

    fun isNearZero(value: Float): Boolean {
        return abs(0F - value) <= 0.0000001F
    }

}