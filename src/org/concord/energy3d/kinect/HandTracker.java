/****************************************************************************
*                                                                           *
*  OpenNI 1.x Alpha                                                         *
*  Copyright (C) 2011 PrimeSense Ltd.                                       *
*                                                                           *
*  This file is part of OpenNI.                                             *
*                                                                           *
*  OpenNI is free software: you can redistribute it and/or modify           *
*  it under the terms of the GNU Lesser General Public License as published *
*  by the Free Software Foundation, either version 3 of the License, or     *
*  (at your option) any later version.                                      *
*                                                                           *
*  OpenNI is distributed in the hope that it will be useful,                *
*  but WITHOUT ANY WARRANTY; without even the implied warranty of           *
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the             *
*  GNU Lesser General Public License for more details.                      *
*                                                                           *
*  You should have received a copy of the GNU Lesser General Public License *
*  along with OpenNI. If not, see <http://www.gnu.org/licenses/>.           *
*                                                                           *
****************************************************************************/
package org.concord.energy3d.kinect;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import org.OpenNI.ActiveHandEventArgs;
import org.OpenNI.Context;
import org.OpenNI.DepthGenerator;
import org.OpenNI.DepthMetaData;
import org.OpenNI.GeneralException;
import org.OpenNI.GestureGenerator;
import org.OpenNI.GestureRecognizedEventArgs;
import org.OpenNI.HandsGenerator;
import org.OpenNI.IObservable;
import org.OpenNI.IObserver;
import org.OpenNI.InactiveHandEventArgs;
import org.OpenNI.OutArg;
import org.OpenNI.Point3D;
import org.OpenNI.ScriptNode;
import org.OpenNI.StatusException;
import org.concord.energy3d.scene.SceneManager;

class HandTracker extends Component
{
	private boolean handTacking = false;

	class MyGestureRecognized implements IObserver<GestureRecognizedEventArgs>
	{

		@Override
		public void update(final IObservable<GestureRecognizedEventArgs> observable,
				final GestureRecognizedEventArgs args)
		{
			System.out.println("Gesture");
			if (!handTacking)
				try
				{
					System.out.println("Start");
					handsGen.StartTracking(args.getEndPosition());
					handTacking = true;
//					gestureGen.removeGesture("Click");
				} catch (final StatusException e) {
					e.printStackTrace();
				}
			else {
				System.out.println("Grab");
				SceneManager.getInstance().grabOrRelease();
			}

		}
	}
	class MyHandCreateEvent implements IObserver<ActiveHandEventArgs>
	{
		@Override
		public void update(final IObservable<ActiveHandEventArgs> observable,
				final ActiveHandEventArgs args)
		{
			System.out.println(args);
			final ArrayList<Point3D> newList = new ArrayList<Point3D>();
			newList.add(args.getPosition());
			history.put(new Integer(args.getId()), newList);
		}
	}
	class MyHandUpdateEvent implements IObserver<ActiveHandEventArgs>
	{
		@Override
		public void update(final IObservable<ActiveHandEventArgs> observable,
				final ActiveHandEventArgs args)
		{
//			System.out.println(args);
			final ArrayList<Point3D> historyList = history.get(args.getId());

			historyList.add(args.getPosition());

			while (historyList.size() > historySize)
			{
				historyList.remove(0);
			}

			SceneManager.getInstance().moveMouse(args.getPosition().getX(), args.getPosition().getY());

		}
	}
	private final int historySize = 10;
	class MyHandDestroyEvent implements IObserver<InactiveHandEventArgs>
	{
		@Override
		public void update(final IObservable<InactiveHandEventArgs> observable,
				final InactiveHandEventArgs args)
		{
			handTacking = false;
			System.out.println(args);
			history.remove(args.getId());
			if (history.isEmpty())
			{
				try
				{
					gestureGen.addGesture("Click");
					gestureGen.addGesture("Wave");
				} catch (final StatusException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

    /**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private OutArg<ScriptNode> scriptNode;
    private Context context;
    private DepthGenerator depthGen;
    private GestureGenerator gestureGen;
    private HandsGenerator handsGen;
    private HashMap<Integer, ArrayList<Point3D>> history;
    private byte[] imgbytes;
    private float histogram[];

    private BufferedImage bimg;
    int width, height;

    private final String SAMPLE_XML_FILE = "C:\\Program Files (x86)\\OpenNI\\Data\\SamplesConfig.xml";
    public HandTracker()
    {

        try {
            scriptNode = new OutArg<ScriptNode>();
            context = Context.createFromXmlFile(SAMPLE_XML_FILE, scriptNode);

            gestureGen = GestureGenerator.create(context);
            gestureGen.addGesture("Wave");
            gestureGen.getGestureRecognizedEvent().addObserver(new MyGestureRecognized());

            handsGen = HandsGenerator.create(context);
            handsGen.getHandCreateEvent().addObserver(new MyHandCreateEvent());
            handsGen.getHandUpdateEvent().addObserver(new MyHandUpdateEvent());
            handsGen.getHandDestroyEvent().addObserver(new MyHandDestroyEvent());

            depthGen = DepthGenerator.create(context);
            final DepthMetaData depthMD = depthGen.getMetaData();

			context.startGeneratingAll();

            history = new HashMap<Integer, ArrayList<Point3D>>();

            histogram = new float[10000];
            width = depthMD.getFullXRes();
            height = depthMD.getFullYRes();

            imgbytes = new byte[width*height];

            final DataBufferByte dataBuffer = new DataBufferByte(imgbytes, width*height);
            final Raster raster = Raster.createPackedRaster(dataBuffer, width, height, 8, null);
            bimg = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
            bimg.setData(raster);

        } catch (final GeneralException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void calcHist(final ShortBuffer depth)
    {
        // reset
        for (int i = 0; i < histogram.length; ++i)
            histogram[i] = 0;

        depth.rewind();

        int points = 0;
        while(depth.remaining() > 0)
        {
            final short depthVal = depth.get();
            if (depthVal != 0)
            {
                histogram[depthVal]++;
                points++;
            }
        }

        for (int i = 1; i < histogram.length; i++)
        {
            histogram[i] += histogram[i-1];
        }

        if (points > 0)
        {
            for (int i = 1; i < histogram.length; i++)
            {
                histogram[i] = (int)(256 * (1.0f - (histogram[i] / points)));
            }
        }
    }


    void updateDepth()
    {
        try {
            final DepthMetaData depthMD = depthGen.getMetaData();

            context.waitAnyUpdateAll();

            final ShortBuffer depth = depthMD.getData().createShortBuffer();
            calcHist(depth);
            depth.rewind();

            while(depth.remaining() > 0)
            {
                final int pos = depth.position();
                final short pixel = depth.get();
                imgbytes[pos] = (byte)histogram[pixel];
            }
        } catch (final GeneralException e) {
            e.printStackTrace();
        }
    }


    @Override
	public Dimension getPreferredSize() {
        return new Dimension(width, height);
    }

    Color colors[] = {Color.RED, Color.BLUE, Color.CYAN, Color.GREEN, Color.MAGENTA, Color.PINK, Color.YELLOW};
    @Override
	public void paint(final Graphics g) {
        final DataBufferByte dataBuffer = new DataBufferByte(imgbytes, width*height);
        final Raster raster = Raster.createPackedRaster(dataBuffer, width, height, 8, null);
        bimg.setData(raster);

        g.drawImage(bimg, 0, 0, null);

        for (final Integer id : history.keySet())
        {
        	try
        	{
        	final ArrayList<Point3D> points = history.get(id);
        	g.setColor(colors[id%colors.length]);
        	final int[] xPoints = new int[points.size()];
        	final int[] yPoints = new int[points.size()];
        	for (int i = 0; i < points.size(); ++i)
        	{
        		final Point3D proj = depthGen.convertRealWorldToProjective(points.get(i));
        		xPoints[i] = (int)proj.getX();
        		yPoints[i] = (int)proj.getY();
        	}
            g.drawPolyline(xPoints, yPoints, points.size());
    		final Point3D proj = depthGen.convertRealWorldToProjective(points.get(points.size()-1));
            g.drawArc((int)proj.getX(), (int)proj.getY(), 5, 5, 0, 360);
        	} catch (final StatusException e)
        	{
        		e.printStackTrace();
        	}
        }

    }
}