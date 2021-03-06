package calculatorapp.free.quick.com.jigsawsample

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Path
import android.graphics.Point
import android.support.annotation.RawRes
import android.util.SparseArray
import org.json.JSONObject

/**
 * 创建时间： 2019/4/9
 * 作者：yanyinan
 * 功能描述：一个模板的信息提供者
 *
 */
class TemplateInfoProvider(context: Context, @RawRes resId: Int) {
    private val mResId = resId
    private val jsonHollow: JSONObject

    init {
        val jsonString = readFile(context, mResId)
        jsonHollow = JSONObject(jsonString)
    }

    companion object {
        private const val IS_REGULAR_JSON_KEY = "isRegular"
        private const val HOLLOW_JSON_KEY = "hollows"
        private const val CIRCLE_JSON_KEY = "circles"
        private const val CONTROL_JSON_KEY = "controls"
        private const val WIDTH_JSON_KEY = "width"
        private const val HEIGHT_JSON_KEY = "height"
    }

    fun getJigsawHeightWidthRatio(): Float {
        val width = jsonHollow.optDouble(WIDTH_JSON_KEY)
        val height = jsonHollow.optDouble(HEIGHT_JSON_KEY)
        val ratio = height / width
        return ratio.toFloat()
    }

    fun getIsRegular(): Boolean {
        return jsonHollow.optBoolean(IS_REGULAR_JSON_KEY)
    }

    /**
     * @param standLength:单位长度
     */
    fun getPictureModelList(bitmapList: List<Bitmap>, standLength: Int): List<PictureModel> {
        val pictureList = mutableListOf<PictureModel>()
        val isRegular = jsonHollow.optBoolean(IS_REGULAR_JSON_KEY)
        val width = jsonHollow.optDouble(WIDTH_JSON_KEY)
        val height = jsonHollow.optDouble(HEIGHT_JSON_KEY)
        val ratio = height / width
        val hollowList = getHollowListByJsonFile(standLength, jsonHollow, isRegular, ratio.toFloat())
        hollowList.forEachIndexed { index, hollowModel ->
            val pictureModel = PictureModel(bitmapList[index], hollowModel)
            pictureList.add(pictureModel)
        }
        if (isRegular) {
            handleEffectPicModel(jsonHollow, pictureList)
        }
        return pictureList
    }

    private fun handleEffectPicModel(jsonObject: JSONObject, pictureList: MutableList<PictureModel>) {
        val controls = jsonObject.optJSONArray(CONTROL_JSON_KEY)
        controls?.let {
            for (i in 0 until controls.length()) {
                val leftMap = SparseArray<List<PictureModel>>()
                val topMap = SparseArray<List<PictureModel>>()
                val rightMap = SparseArray<List<PictureModel>>()
                val bottomMap = SparseArray<List<PictureModel>>()

                val hollowLocationStr = controls[i] as? String
                //当前的PictureModel
                val currentPictureModel = pictureList[i]

                val effectHollowArray = hollowLocationStr?.split(" ")

                val picListLeftRight = mutableListOf<PictureModel>()
                val picListLeftLeft = mutableListOf<PictureModel>()
                val picListRightLeft = mutableListOf<PictureModel>()
                val picListRightRight = mutableListOf<PictureModel>()
                val picListTopBottom = mutableListOf<PictureModel>()
                val picListTopTop = mutableListOf<PictureModel>()
                val picListBottomTop = mutableListOf<PictureModel>()
                val picListBottomBottom = mutableListOf<PictureModel>()

                effectHollowArray?.forEach { effect ->
                    val effectFactor = effect.split(",")
                    //当前Hollow拖动的边
                    val currentDirection = effectFactor[0].toInt()
                    //联动到的Hollow
                    val targetHollowIndex = effectFactor[1].toInt()
                    //联动到的Hollow被联动的边
                    val targetHollowDirection = effectFactor[2].toInt()

                    //被联动的PictureModel
                    val targetPictureModel = pictureList[targetHollowIndex]

                    when (currentDirection) {

                        HollowModel.LEFT -> {
                            when (targetHollowDirection) {
                                HollowModel.LEFT -> {
                                    picListLeftLeft.add(targetPictureModel)
                                }

                                HollowModel.RIGHT -> {
                                    picListLeftRight.add(targetPictureModel)
                                }

                            }
                        }

                        HollowModel.TOP -> {
                            when (targetHollowDirection) {
                                HollowModel.TOP -> {
                                    picListTopTop.add(targetPictureModel)
                                }

                                HollowModel.BOTTOM -> {
                                    picListTopBottom.add(targetPictureModel)
                                }

                            }
                        }

                        HollowModel.RIGHT -> {
                            when (targetHollowDirection) {
                                HollowModel.LEFT -> {
                                    picListRightLeft.add(targetPictureModel)
                                }

                                HollowModel.RIGHT -> {
                                    picListRightRight.add(targetPictureModel)
                                }

                            }
                        }

                        HollowModel.BOTTOM -> {
                            when (targetHollowDirection) {
                                HollowModel.TOP -> {
                                    picListBottomTop.add(targetPictureModel)
                                }

                                HollowModel.BOTTOM -> {
                                    picListBottomBottom.add(targetPictureModel)
                                }

                            }
                        }
                    }

                }

                if (picListLeftLeft.size > 0) {
                    leftMap.put(HollowModel.LEFT, picListLeftLeft)
                }
                if (picListLeftRight.size > 0) {
                    leftMap.put(HollowModel.RIGHT, picListLeftRight)
                }
                if (leftMap.size() > 0) {
                    currentPictureModel.addEffectPictureModel(leftMap, HollowModel.LEFT)
                }

                if (picListTopTop.size > 0) {
                    topMap.put(HollowModel.TOP, picListTopTop)
                }
                if (picListTopBottom.size > 0) {
                    topMap.put(HollowModel.BOTTOM, picListTopBottom)
                }
                if (topMap.size() > 0) {
                    currentPictureModel.addEffectPictureModel(topMap, HollowModel.TOP)
                }


                if (picListRightLeft.size > 0) {
                    rightMap.put(HollowModel.LEFT, picListRightLeft)
                }
                if (picListRightRight.size > 0) {
                    rightMap.put(HollowModel.RIGHT, picListRightRight)
                }

                if (rightMap.size() > 0) {
                    currentPictureModel.addEffectPictureModel(rightMap, HollowModel.RIGHT)
                }

                if (picListBottomTop.size > 0) {
                    bottomMap.put(HollowModel.TOP, picListBottomTop)
                }
                if (picListBottomBottom.size > 0) {
                    bottomMap.put(HollowModel.BOTTOM, picListBottomBottom)
                }

                if (bottomMap.size() > 0) {
                    currentPictureModel.addEffectPictureModel(bottomMap, HollowModel.BOTTOM)
                }

                currentPictureModel.initCanDragDirectionList()
            }
        }
    }

    private fun getHollowListByJsonFile(widthStandLength: Int, jsonObject: JSONObject, regular: Boolean, ratio: Float): List<HollowModel> {
        val hollowList = mutableListOf<HollowModel>()

        val circleArray = jsonObject.optJSONArray(CIRCLE_JSON_KEY)
        val heightStandLength = widthStandLength * ratio
        if (circleArray != null) {
            for (i in 0 until circleArray.length()) {
                val circleLocationStr = circleArray[i] as? String
                val positionArrayForOneCircle = circleLocationStr?.split(" ")
                //这里使用“!!”是因为模板为自己提供，如果为空说明模板有错
                val circleCenterX = positionArrayForOneCircle!![0].split(",")[0].toFloat() * widthStandLength
                val circleCenterY = positionArrayForOneCircle[0].split(",")[1].toFloat() * heightStandLength
                val radius = positionArrayForOneCircle[1].toFloat() * widthStandLength
                val hollowPath = Path()
                hollowPath.addCircle(circleCenterX, circleCenterY, radius, Path.Direction.CW)
                //通过hollowLocationStr求出外接矩形，将外接矩形数据写入HollowModel
                val pointHollowArray = getPointsHollowForCircle(circleCenterX, circleCenterY, radius)
                val hollow = HollowModel(pointHollowArray[0], pointHollowArray[1], pointHollowArray[2] - pointHollowArray[0]
                        , pointHollowArray[3] - pointHollowArray[1], hollowPath, Point(circleCenterX.toInt(), circleCenterY.toInt()))
                hollowList.add(hollow)
            }
        } else {
            //非圆形
            val hollowArray = jsonObject.optJSONArray(HOLLOW_JSON_KEY)
            val hollowPointArray = mutableListOf<Point>()
            hollowArray?.let {
                for (i in 0 until hollowArray.length()) {
                    hollowPointArray.clear()

                    val hollowLocationStr = hollowArray[i] as? String
                    val positionArrayForOneHollow = hollowLocationStr?.split(" ")

                    positionArrayForOneHollow?.forEach { p ->
                        val xAndY = p.split(",")
                        val x = xAndY[0].toFloat() * widthStandLength
                        val y = xAndY[1].toFloat() * heightStandLength
                        val point = Point(x.toInt(), y.toInt())
                        hollowPointArray.add(point)
                    }

                    val hollowPath = Path()
                    if (!regular) {
                        hollowPointArray.forEachIndexed { index, point ->
                            if (index == 0) {
                                hollowPath.moveTo(point.x.toFloat(), point.y.toFloat())
                            } else {
                                hollowPath.lineTo(point.x.toFloat(), point.y.toFloat())
                            }
                        }
                        //闭合
                        hollowPath.close()
                    }

                    val pointHollowArray = getPointsHollow(hollowPointArray)

                    //通过hollowLocationStr求出外接矩形，将外接矩形数据写入HollowModel
                    val hollow = if (regular) {
                        HollowModel(pointHollowArray[0], pointHollowArray[1], pointHollowArray[2] - pointHollowArray[0]
                                , pointHollowArray[3] - pointHollowArray[1])
                    } else {
                        val centerPoint = getCenterPoint(hollowPointArray)
                        HollowModel(pointHollowArray[0], pointHollowArray[1], pointHollowArray[2] - pointHollowArray[0]
                                , pointHollowArray[3] - pointHollowArray[1], hollowPath, centerPoint)
                    }

                    hollowList.add(hollow)
                }
            }
        }

        return hollowList
    }

    private fun getCenterPoint(mPoints: List<Point>): Point {
        val size = mPoints.size
        var accumulateX = 0
        var accumulateY = 0
        mPoints.forEach {
            accumulateX += it.x
            accumulateY += it.y
        }

        val centerX: Int = accumulateX / size
        val centerY: Int = accumulateY / size
        return Point(centerX, centerY)
    }


    /**
     * 拿到各个顶点对应的图形的外接矩形的坐标点
     */
    private fun getPointsHollow(hollowPointArray: MutableList<Point>): Array<Float> {
        var left: Float = -1f
        var top: Float = -1f
        var right: Float = -1f
        var bottom: Float = -1f
        hollowPointArray.forEach { point ->
            val x = point.x
            val y = point.y
            if (x < left || left == -1f) {
                left = x.toFloat()
            }

            if (x > right || right == -1f) {
                right = x.toFloat()
            }

            if (y < top || top == -1f) {
                top = y.toFloat()
            }

            if (y > bottom || bottom == -1f) {
                bottom = y.toFloat()
            }
        }
        return arrayOf(left, top, right, bottom)
    }

    private fun getPointsHollowForCircle(circleCenterX: Float, circleCenterY: Float, radius: Float): Array<Float> {
        val left: Float = (circleCenterX - radius)
        val top: Float = (circleCenterY - radius)
        val right: Float = (circleCenterX + radius)
        val bottom: Float = (circleCenterY + radius)

        return arrayOf(left, top, right, bottom)
    }
}