package com.stripe1.xmouse;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

public class MyCanvasView extends View {

    enum ClickType {
        Left_click,
        Middle_click,
        Right_click,
        Drag_Down,
        Drag_Up,
        Zoom_in,
        Zoom_out
    }

    private class Pointer {
        public final int id;
        public float x;
        public float y;
        public final float pressure;
        public final float size;

        private Pointer(int id, float x, float y, float pressure, float size) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.pressure = pressure;
            this.size = size;
        }
        private Pointer(Pointer pointer) {
            this.id = pointer.id;
            this.x = pointer.x;
            this.y = pointer.y;
            this.pressure = pointer.pressure;
            this.size = pointer.size;
        }
    }

    private enum Paints {
        Empty(0),
        Base(1),
        Deadzone(2),
        Hat(3),
        VScrollBorder(4),
        HScrollBorder(5),
        VScrollTrail(6),
        HScrollTrail(7),
        Trail(8),
        Drag(9),
        Zoom(10),
        Marker(11),
        MarkerDrag(12),
        MarkerZoom(13),
        MarkerhScroll(14),
        MarkervScroll(15),
        AbsoluteBorder(16),
        AbsoluteTrail(17),
        AbsoluteMarker(18);

        public final int id;

        Paints(int id) {
            this.id = id;
        }
    }

    private static ArrayList<Paint> paints;

    private float cx;
    private float cy;

    private float hx;
    private float hy;

    private float cs;
    private float dzs;
    private float hs;

    private float js_sensitivity;


    private float r=80;
    private float rz=80;
    private float rs=80;

    private Path mPath;

    private float vscrollRight = 120f; //pixels to draw from right, for vertical scroll bar
    private float vscrollTop = 120f; //pixels to draw from top, for vertical scroll bar
    private float vscrollBottom = 120f; //pixels to draw from bottom, for vertical scroll bar

    private float hscrollTop = 120f; //pixels to draw from top, for horizontal scroll bar
    private float hscrollLeft = 120f; //pixels to draw from left, for horizontal scroll bar
    private float hscrollRight = 120f; //pixels to draw from right, for horizontal scroll bar

    final float clickMovementThreshold = 3; ///TODO: x, y, ?circle
    final float dragMovementTreshold = 18; ///TODO: x, y, ?circle
    final float movementTreshold = 4; ///TODO: x, y, ?circle
    final double zoomTreshold = 0.5;
    private int w=0;
    private int h=0;
    private Pointer start;
    private long downStart = 0;
    private long downEnd = 0;
    private boolean dragging = false;
    private boolean draggable = true;
    private int zoomCounter = 0;
    private int zoomOverFlow = 10;
    private final float SCROLL_TOLERANCE = 20;
    private boolean vscrolling = false;
    private boolean hscrolling = false;
    private double scaleFactor = 1;
    private boolean firstTouch= true;
    private boolean zooming =false;
    private double dist = 0;
    private Pointer curr;
    private Pointer old;
    private Pointer sec;
    private boolean twoFingerScroll = false;
    private int pointercount = 0;
    private boolean touching;
    private float newDist = 0;


    // coordinate rounding errors
    private float reX = 0;
    private float reY = 0;

    public MyCanvasView(Context context) {
        super(context);

        mPath=new Path();

        paints= new ArrayList<>();

        //Empty(0)

        paints.add(new Paint());
        paints.get(0).setAntiAlias(true);
        paints.get(0).setDither(true);
        paints.get(0).setColor(Color.TRANSPARENT);
        paints.get(0).setStrokeWidth(3);
        paints.get(0).setStyle(Paint.Style.FILL);
        paints.get(0).setStrokeJoin(Paint.Join.MITER);
        paints.get(0).setStrokeCap(Paint.Cap.SQUARE);
        paints.get(0).setPathEffect(null);

        //Base(1)
        paints.add(new Paint());
        paints.get(1).setAntiAlias(true);
        paints.get(1).setDither(true);
        paints.get(1).setColor(Color.BLACK);
        paints.get(1).setStrokeWidth(3);
        paints.get(1).setStyle(Paint.Style.STROKE);
        paints.get(1).setStrokeJoin(Paint.Join.MITER);
        paints.get(1).setStrokeCap(Paint.Cap.SQUARE);
        paints.get(1).setPathEffect(null);


        //Deadzone(2)
        paints.add(new Paint());
        paints.get(2).setAntiAlias(true);
        paints.get(2).setDither(true);
        paints.get(2).setColor(Color.GRAY);
        paints.get(2).setStrokeWidth(1);
        paints.get(2).setStyle(Paint.Style.FILL_AND_STROKE);
        paints.get(2).setStrokeJoin(Paint.Join.MITER);
        paints.get(2).setStrokeCap(Paint.Cap.SQUARE);
        paints.get(2).setPathEffect(null);

        //Hat(3)
        paints.add(new Paint());
        paints.get(3).setAntiAlias(true);
        paints.get(3).setDither(true);
        paints.get(3).setColor(Color.argb(0xC0,0,0,0));
        paints.get(3).setStrokeWidth(3);
        paints.get(3).setStyle(Paint.Style.FILL_AND_STROKE);
        paints.get(3).setStrokeJoin(Paint.Join.MITER);
        paints.get(3).setStrokeCap(Paint.Cap.SQUARE);
        paints.get(3).setPathEffect(null);


        //VScrollBorder(4)
        paints.add(new Paint());
        paints.get(4).setAntiAlias(true);
        paints.get(4).setDither(true);
        paints.get(4).setColor(Color.BLUE);
        paints.get(4).setStrokeWidth(6);
        paints.get(4).setStyle(Paint.Style.STROKE);
        paints.get(4).setStrokeJoin(Paint.Join.MITER);
        paints.get(4).setStrokeCap(Paint.Cap.SQUARE);
        paints.get(4).setPathEffect(new DashPathEffect(new float[]{10, 20}, 0));

        //HScrollBorder(5)
        paints.add(new Paint());
        paints.get(5).setAntiAlias(true);
        paints.get(5).setDither(true);
        paints.get(5).setColor(Color.GREEN);
        paints.get(5).setStrokeWidth(6);
        paints.get(5).setStyle(Paint.Style.STROKE);
        paints.get(5).setStrokeJoin(Paint.Join.MITER);
        paints.get(5).setStrokeCap(Paint.Cap.SQUARE);
        paints.get(5).setPathEffect(new DashPathEffect(new float[]{10, 20}, 0));

        //VScrollTrail(6)
        paints.add(new Paint());
        paints.get(6).setAntiAlias(true);
        paints.get(6).setDither(true);
        paints.get(6).setColor(Color.BLUE);
        paints.get(6).setStrokeWidth(4);
        paints.get(6).setStyle(Paint.Style.STROKE);
        paints.get(6).setStrokeJoin(Paint.Join.MITER);
        paints.get(6).setStrokeCap(Paint.Cap.ROUND);
        paints.get(6).setPathEffect(new DashPathEffect(new float[]{10, 20}, 0));

        //HScrollTrail(7)
        paints.add(new Paint());
        paints.get(7).setAntiAlias(true);
        paints.get(7).setDither(true);
        paints.get(7).setColor(Color.GREEN);
        paints.get(7).setStrokeWidth(4);
        paints.get(7).setStyle(Paint.Style.STROKE);
        paints.get(7).setStrokeJoin(Paint.Join.MITER);
        paints.get(7).setStrokeCap(Paint.Cap.ROUND);
        paints.get(7).setPathEffect(new DashPathEffect(new float[]{10, 20}, 0));

        //Trail(8)
        paints.add(new Paint());
        paints.get(8).setAntiAlias(true);
        paints.get(8).setDither(true);
        paints.get(8).setColor(Color.RED);
        paints.get(8).setStrokeWidth(7);
        paints.get(8).setStyle(Paint.Style.STROKE);
        paints.get(8).setStrokeJoin(Paint.Join.ROUND);
        paints.get(8).setStrokeCap(Paint.Cap.ROUND);
        paints.get(8).setPathEffect(null);

        //Drag(9)
        paints.add(new Paint());
        paints.get(9).setAntiAlias(true);
        paints.get(9).setDither(true);
        paints.get(9).setColor(Color.YELLOW);
        paints.get(9).setStrokeWidth(5);
        paints.get(9).setStyle(Paint.Style.STROKE);
        paints.get(9).setStrokeJoin(Paint.Join.ROUND);
        paints.get(9).setStrokeCap(Paint.Cap.ROUND);
        paints.get(9).setPathEffect(null);

        //Zoom(10)
        paints.add(new Paint());
        paints.get(10).setAntiAlias(true);
        paints.get(10).setDither(true);
        paints.get(10).setColor(Color.YELLOW);
        paints.get(10).setStrokeWidth(6);
        paints.get(10).setStyle(Paint.Style.STROKE);
        paints.get(10).setStrokeJoin(Paint.Join.ROUND);
        paints.get(10).setStrokeCap(Paint.Cap.ROUND);
        paints.get(10).setPathEffect(null);

        //Marker(11)
        paints.add(new Paint());
        paints.get(11).setAntiAlias(true);
        paints.get(11).setDither(true);
        paints.get(11).setColor(Color.BLACK);
        paints.get(11).setStrokeWidth(5);
        paints.get(11).setStyle(Paint.Style.STROKE);
        paints.get(11).setStrokeJoin(Paint.Join.ROUND);
        paints.get(11).setStrokeCap(Paint.Cap.ROUND);
        paints.get(11).setPathEffect(null);

        //MarkerDrag(12)
        paints.add(new Paint());
        paints.get(12).setAntiAlias(true);
        paints.get(12).setDither(true);
        paints.get(12).setColor(Color.BLACK);
        paints.get(12).setStrokeWidth(10);
        paints.get(12).setStyle(Paint.Style.STROKE);
        paints.get(12).setStrokeJoin(Paint.Join.ROUND);
        paints.get(12).setStrokeCap(Paint.Cap.ROUND);
        paints.get(12).setPathEffect(null);

        //MarkerZoom(13)
        paints.add(new Paint());
        paints.get(13).setAntiAlias(true);
        paints.get(13).setDither(true);
        paints.get(13).setColor(Color.BLACK);
        paints.get(13).setStrokeWidth(10);
        paints.get(13).setStyle(Paint.Style.STROKE);
        paints.get(13).setStrokeJoin(Paint.Join.ROUND);
        paints.get(13).setStrokeCap(Paint.Cap.ROUND);
        paints.get(13).setPathEffect(null);

        //MarkerhScroll(14)
        paints.add(new Paint());
        paints.get(14).setAntiAlias(true);
        paints.get(14).setDither(true);
        paints.get(14).setColor(Color.BLACK);
        paints.get(14).setStrokeWidth(6);
        paints.get(14).setStyle(Paint.Style.STROKE);
        paints.get(14).setStrokeJoin(Paint.Join.ROUND);
        paints.get(14).setStrokeCap(Paint.Cap.ROUND);
        paints.get(14).setPathEffect(new DashPathEffect(new float[]{50, 50}, 0));

        //MarkervScroll(15)
        paints.add(new Paint());
        paints.get(15).setAntiAlias(true);
        paints.get(15).setDither(true);
        paints.get(15).setColor(Color.BLACK);
        paints.get(15).setStrokeWidth(6);
        paints.get(15).setStyle(Paint.Style.STROKE);
        paints.get(15).setStrokeJoin(Paint.Join.ROUND);
        paints.get(15).setStrokeCap(Paint.Cap.ROUND);
        paints.get(15).setPathEffect(new DashPathEffect(new float[]{50, 50}, 0));

        //AbsoluteBorder(16)
        paints.add(new Paint());
        paints.get(16).setAntiAlias(true);
        paints.get(16).setDither(true);
        paints.get(16).setColor(Color.BLACK);
        paints.get(16).setStrokeWidth(3);
        paints.get(16).setStyle(Paint.Style.STROKE);
        paints.get(16).setStrokeJoin(Paint.Join.MITER);
        paints.get(16).setStrokeCap(Paint.Cap.SQUARE);
        paints.get(16).setPathEffect(new DashPathEffect(new float[]{5, 10}, 0));

        //AbsoluteTrail(17)
        paints.add(new Paint());
        paints.get(17).setAntiAlias(true);
        paints.get(17).setDither(true);
        paints.get(17).setColor(Color.BLACK);
        paints.get(17).setStrokeWidth(3);
        paints.get(17).setStyle(Paint.Style.STROKE);
        paints.get(17).setStrokeJoin(Paint.Join.ROUND);
        paints.get(17).setStrokeCap(Paint.Cap.ROUND);
        paints.get(17).setPathEffect(null);

        //AbsoluteMarker(18)
        paints.add(new Paint());
        paints.get(18).setAntiAlias(true);
        paints.get(18).setDither(true);
        paints.get(18).setColor(Color.BLACK);
        paints.get(18).setStrokeWidth(3);
        paints.get(18).setStyle(Paint.Style.STROKE);
        paints.get(18).setStrokeJoin(Paint.Join.ROUND);
        paints.get(18).setStrokeCap(Paint.Cap.ROUND);
        paints.get(18).setPathEffect(null);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        cx=getWidth()/2;
        cy=getHeight()/2;
        hx=cx;
        hy=cy;
        float m=Math.min(w,h);
        cs= (int) (m*MainActivity.setting_js_size);
        dzs= (int) (m*MainActivity.setting_js_dead_zone);
        hs=(cs+dzs)/2;


        this.w=w;
        this.h=h;

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        switch (MainActivity.setting_pointing_device) {
            case 1:
                drawTrackpad(canvas);
                break;
            case 2:
                drawJoystick(canvas);
                break;
            case 3:
                drawAbsolute(canvas);
                break;
        }
        invalidate();
    }

    private void drawTrackpad(Canvas canvas) {
        drawScroll(canvas);

        drawTrail(canvas);

        drawMarkers(canvas);

    }


    private void drawScroll(Canvas canvas) {
        //vscrollTop=h*0.1f;
        //vscrollBottom=h*0.1f;
        canvas.drawLine(w- vscrollRight, vscrollTop, w- vscrollRight, h-vscrollBottom, paints.get(Paints.VScrollBorder.id));
        //hscrollLeft=w*0.1f;
        //hscrollRight=w*0.1f;
        canvas.drawLine(hscrollLeft, hscrollTop, w-hscrollRight, hscrollTop, paints.get(Paints.HScrollBorder.id));
    }

    private void drawTrail(Canvas canvas) {
        if(dragging){
            canvas.drawPath(mPath,paints.get(Paints.Drag.id));
        }
        else if(zooming) {
            canvas.drawPath(mPath,paints.get(Paints.Zoom.id));
        }
        else if (vscrolling) {
            canvas.drawPath(mPath,paints.get(Paints.VScrollTrail.id));
        }
        else if (hscrolling) {
            canvas.drawPath(mPath,paints.get(Paints.HScrollTrail.id));
        }
        else if (twoFingerScroll) {
            canvas.drawPath(mPath,paints.get(Paints.Zoom.id));//?
        }
        else {
            canvas.drawPath(mPath,paints.get(Paints.Trail.id));
        }
    }

    private void drawMarkers(Canvas canvas) {
        if(touching) {
            if (dragging) {
                canvas.drawCircle(curr.x, curr.y, r, paints.get(Paints.MarkerDrag.id));
            } else {
                if (!vscrolling && !hscrolling) {
                    if (!zooming) {
                        if (twoFingerScroll) {
                            canvas.drawCircle(curr.x, curr.y, r, paints.get(Paints.MarkerZoom.id));//?
                            canvas.drawCircle(sec.x, sec.y,rz, paints.get(Paints.MarkerZoom.id));//?
                        } else {
                            canvas.drawCircle(curr.x, curr.y, r, paints.get(Paints.Marker.id));
                        }
                    }
                    else {
                        canvas.drawCircle(curr.x, curr.y,rz, paints.get(Paints.MarkerZoom.id));
                        canvas.drawCircle(sec.x, sec.y,rz, paints.get(Paints.MarkerZoom.id));
                    }
                }
                else if(hscrolling) {
                    canvas.drawCircle(curr.x, curr.y, rs, paints.get(Paints.MarkerhScroll.id));
                }
                else {
                    canvas.drawCircle(curr.x, curr.y, rs, paints.get(Paints.MarkervScroll.id));
                }
            }
        }
    }



    private void drawJoystick(Canvas canvas) {
        canvas.drawRect(cx-cs/2, cy-cs/2, cx+cs/2, cy+cs/2, paints.get(Paints.Base.id));
        if (MainActivity.setting_js_dead_zone>0) {
            canvas.drawRect(cx-dzs/2, cy-dzs/2, cx+dzs/2, cy+dzs/2,paints.get(Paints.Deadzone.id));
        }
        canvas.drawRect(hx-hs/2, hy-hs/2, hx+hs/2, hy+hs/2,paints.get(Paints.Hat.id));
    }

    private void drawAbsolute(Canvas canvas) {
        //TODO
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        pointercount=event.getPointerCount();
        curr=new Pointer(event.getPointerId(
                event.getActionIndex()),
                event.getX(event.getActionIndex()),
                event.getY(event.getActionIndex()),
                event.getPressure(event.getActionIndex()),
                event.getSize(event.getActionIndex())
        );
        if (pointercount>1) {
            sec=new Pointer(
                    event.getPointerId(event.getActionIndex()==1?0:1),
                    event.getX(event.getActionIndex()==1?0:1),
                    event.getY(event.getActionIndex()==1?0:1),
                    event.getPressure(event.getActionIndex()==1?0:1),
                    event.getSize(event.getActionIndex()==1?0:1)
            );
        }
        switch (MainActivity.setting_pointing_device) {
            case 1:
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        downStart = System.currentTimeMillis();
                        if (downStart - downEnd < MainActivity.setting_mouse_mdelay) {
                            dragging = true;
                            canvas_touch_move(true);
                            mPath.moveTo(curr.x,curr.y);
                            OnXMouseClicked(ClickType.Drag_Down);
                        }
                        start=new Pointer(curr);
                        canvas_touch_start();
                        invalidate();
                        break;
                    case MotionEvent.ACTION_UP:
                        int dx = (int) Math.abs(curr.x - start.x);
                        int dy = (int) Math.abs(curr.y - start.y);
                        if (dx < clickMovementThreshold && dy < clickMovementThreshold) {
                            if (vscrolling) {
                                OnXMouseClicked(ClickType.Right_click);
                            }
                            else if (hscrolling) {
                                OnXMouseClicked(ClickType.Middle_click);
                            }
                            else {
                                OnXMouseClicked(ClickType.Left_click);
                            }
                        }

                        if (dragging) {
                            dragging = false;
                            OnXMouseClicked(ClickType.Drag_Up);
                            if (MainActivity.setting_mouse_mdelay > 0) {
                                downEnd = System.currentTimeMillis();
                            }
                        } else {
                            downEnd = 0;
                        }

                        vscrolling=false;
                        hscrolling=false;
                        firstTouch = true;
                        zooming = false;
                        draggable = true;
                        twoFingerScroll = false;
                        touch_up(downEnd > 0);
                        invalidate();
                        
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int dx1 = (int) Math.abs(curr.x - start.x);
                        int dy1 = (int) Math.abs(curr.y - start.y);

                        if (event.getPointerCount() > 1) {

                            newDist = (float) Math.sqrt(Math.pow(sec.x - curr.x, 2) + Math.pow(sec.y - curr.y, 2));
                            if (firstTouch) {
                                dist = newDist;
                                firstTouch = false;
                            }

                            scaleFactor = (newDist - dist) / dist;

                            if (Math.abs(scaleFactor) > zoomTreshold) {
                                zooming = true;
                                zoomCounter++;
                                if (zoomCounter > zoomOverFlow) {

                                    if (scaleFactor > 0) {
                                        OnXMouseClicked(ClickType.Zoom_in);
                                    } else {
                                        OnXMouseClicked(ClickType.Zoom_out);
                                    }
                                    zoomCounter = 0;
                                }

                            } else if (!zooming) {
                                twoFingerScroll = true;
                                //scrolling = true;

                            }
                        }
                        if (draggable) {
                            long thisTime = System.currentTimeMillis() - downStart;
                            if (dx1 < dragMovementTreshold && dy1 < dragMovementTreshold) {
                                if (!dragging && thisTime > MainActivity.setting_mouse_delay) {
                                    OnXMouseClicked(ClickType.Drag_Down);
                                    dragging = true;
                                }
                            }
                            else {
                                draggable = false;
                            }
                        }
                        canvas_touch_move(false);

                        invalidate();


                        break;
                }

                break;
            case 2:
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE:

                        float  Mx=cx+cs/2;
                        float  mx=cx-cs/2;
                        float  My=cy+cs/2;
                        float  my=cy-cs/2;

                        float  x=curr.x>Mx?Mx: Math.max(curr.x, mx);
                        float  y=curr.y>My?My: Math.max(curr.y, my);


                        float  Mdx=cx+dzs/2;
                        float  mdx=cx-dzs/2;
                        float  Mdy=cy+dzs/2;
                        float  mdy=cy-dzs/2;

                        boolean dz=x<Mdx&&x>mdx&&y<Mdy&&y>mdy;
                        hx=dz?cx:x;
                        hy=dz?cy:y;

                        jsmove();
                        invalidate();

                        break;
                    case MotionEvent.ACTION_UP:
                        hx=cx;
                        hy=cy;

                        invalidate();

                        break;


                }

                break;
            case 3:
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:

                        //TODO
                        break;
                    case MotionEvent.ACTION_UP:
                        //TODO
                        break;
                    case MotionEvent.ACTION_MOVE:
                        //TODO
                        break;
                }

                //TODO handle event
                break;
        }
        invalidate();
        return true;
    }




    private void canvas_touch_start() {
        touching=true;
        if((start.x>=w-vscrollRight) && (start.y<h-vscrollTop) && (start.y>vscrollBottom)) {
            vscrolling=true;
            start.x = w-vscrollRight/2;
        }
        else if((start.y<=hscrollTop) && (start.x>hscrollLeft) && (start.x<w-hscrollRight)){
            hscrolling=true;
            start.y = hscrollTop/2;

        }
        else {
            vscrolling=false;
            hscrolling=false;
        }

        mPath.reset();
        mPath.moveTo(curr.x,curr.y);
        old=new Pointer(start);
    }
    
    private void canvas_touch_move(boolean silent) {

        if (pointercount>1) {
            vscrolling=false;
            hscrolling=false;
        }
        if (vscrolling) {
            curr.x = w - vscrollRight / 2;
        }
        if (hscrolling) {
            curr.y = hscrollTop / 2;
        }

        float rx = curr.x - old.x;
        float ry = curr.y - old.y;
        float dx = Math.abs(rx);
        float dy = Math.abs(ry);


        if (!(zooming||twoFingerScroll)) {

            if (dx >= movementTreshold || dy >= movementTreshold) {
                if(!silent) mPath.quadTo(old.x, old.y, (curr.x+old.x)/2, (curr.y+old.y)/2);
                old=new Pointer(curr);

                OnXMouseMoved(rx, ry);
            }
        }else{
            mPath.reset();
            mPath.setLastPoint(curr.x,curr.y);
            if(!silent) {//?
                mPath.lineTo(sec.x,sec.y);
                paints.get(Paints.Zoom.id).setStrokeWidth(newDist/100);
            }
        }

    }
    private void touch_up(boolean linger) {
        mPath.lineTo(old.x,old.y);
        // kill this so we don't double draw
        if(!linger) mPath.reset();
        vscrolling=false;
        hscrolling=false;
        touching=false;
    }



    public void OnXMouseMoved(float dx, float dy) {

        dx=dx*MainActivity.setting_mouse_sensitivity;
        dy=dy*MainActivity.setting_mouse_sensitivity;

        dx += reX;
        dy += reY;
        reX = dx - Math.round(dx);
        reY = dy - Math.round(dy);
        dx -= reX;
        dy -= reY;

        String cmd="";
        if(dx<0 && dy <0){
            if(vscrolling){
                if(MainActivity.setting_invert_scroll){
                    cmd="xdotool click 4";
                }else {
                    cmd = "xdotool click 5";
                }
            }
            else if(hscrolling){
                if(MainActivity.setting_invert_scroll){
                    cmd="xdotool click 7";
                }else {
                    cmd = "xdotool click 6";
                }
            }
            else {
                cmd="xdotool mousemove_relative -- "+dx+" "+dy;
            }
        }
        else if(dx<0 && dy>0){
            if(vscrolling){
                if(MainActivity.setting_invert_scroll){
                    cmd="xdotool click 5";
                }else {
                    cmd = "xdotool click 4";
                }
            }
            else if(hscrolling){
                if(MainActivity.setting_invert_scroll){
                    cmd="xdotool click 7";
                }else {
                    cmd = "xdotool click 6";
                }
            }
            else {
                cmd="xdotool mousemove_relative -- "+dx+" "+dy;
            }
        }
        else if(dx>0 && dy <0){
            if(vscrolling){
                if(MainActivity.setting_invert_scroll){
                    cmd="xdotool click 4";
                }else {
                    cmd = "xdotool click 5";
                }
            }
            else if(hscrolling){
                if(MainActivity.setting_invert_scroll){
                    cmd="xdotool click 6";
                }else {
                    cmd = "xdotool click 7";
                }
            }
            else {
                cmd="xdotool mousemove_relative -- "+dx+" "+dy;
            }
        }
        else if(dx>0 && dy>0){
            if(vscrolling){
                if(MainActivity.setting_invert_scroll){
                    cmd="xdotool click 5";
                }else {
                    cmd = "xdotool click 4";
                }
            }
            else if(hscrolling){
                if(MainActivity.setting_invert_scroll){
                    cmd="xdotool click 6";
                }else {
                    cmd = "xdotool click 7";
                }
            }
            else {
                cmd="xdotool mousemove_relative "+dx+" "+dy;
            }
        }
        else if(dx>0 && dy==0){
            if(hscrolling){
                if(MainActivity.setting_invert_scroll){
                    cmd="xdotool click 6";
                }else {
                    cmd = "xdotool click 7";
                }
            }
            else {
                cmd="xdotool mousemove_relative "+dx+" "+dy;
            }
        }
        else if(dx<0 && dy==0){
            if(hscrolling){
                if(MainActivity.setting_invert_scroll){
                    cmd="xdotool click 7";
                }else {
                    cmd = "xdotool click 6";
                }
            }
            else {
                cmd="xdotool mousemove_relative -- "+dx+" "+dy;
            }
        }
        else if(dx==0 && dy>0){
            if(vscrolling){
                if(MainActivity.setting_invert_scroll){
                    cmd="xdotool click 5";
                }else {
                    cmd = "xdotool click 4";
                }
            }
            else {
                cmd="xdotool mousemove_relative "+dx+" "+dy;
            }
        }
        else if(dx==0 && dy<0){
            if(vscrolling){
                if(MainActivity.setting_invert_scroll){
                    cmd="xdotool click 4";
                }else {
                    cmd = "xdotool click 5";
                }
            }
            else {
                cmd="xdotool mousemove_relative -- "+dx+" "+dy;
            }
        }
        MainActivity.conn.executeShellCommand(cmd);
    }

    public void OnXMouseClicked(ClickType type) {
        String cmd ="";
        switch(type){
            case Left_click:
                cmd ="xdotool click 1";
                break;
            case Middle_click:
                cmd ="xdotool click 2";
                break;
            case Right_click:
                cmd ="xdotool click 3";
                break;
            case Drag_Down:
                cmd ="xdotool mousedown 1";
                break;
            case Drag_Up:
                cmd ="xdotool mouseup 1";
                break;
            case Zoom_in:
                cmd="xdotool key Ctrl+plus";
                break;
            case Zoom_out:
                cmd="xdotool key Ctrl+minus";
                break;
            default:
                break;
        }
        MainActivity.conn.executeShellCommand(cmd);

    }


    public void jsmove() {

        float dx=(hx -cx)/cs*MainActivity.setting_js_sensitivity*100;
        float dy=(hy -cy)/cs*MainActivity.setting_js_sensitivity*100;

        dx += reX;
        dy += reY;
        reX = dx - Math.round(dx);
        reY = dy - Math.round(dy);
        dx -= reX;
        dy -= reY;

        String cmd="";
        if(dx <0 || dy <0){
            cmd="xdotool mousemove_relative -- "+dx+" "+dy;
        }else{
            cmd="xdotool mousemove_relative "+dx+" "+dy;
        }
        MainActivity.conn.executeShellCommand(cmd);
    }
}