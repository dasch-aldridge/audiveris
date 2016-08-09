//------------------------------------------------------------------------------------------------//
//                                                                                                //
//                            I m a g e F o r m a t E x c e p t i o n                             //
//                                                                                                //
//------------------------------------------------------------------------------------------------//
// <editor-fold defaultstate="collapsed" desc="hdr">
//
//  Copyright © Hervé Bitteur and others 2000-2016. All rights reserved.
//
//  This program is free software: you can redistribute it and/or modify it under the terms of the
//  GNU Affero General Public License as published by the Free Software Foundation, either version
//  3 of the License, or (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
//  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//  See the GNU Affero General Public License for more details.
//
//  You should have received a copy of the GNU Affero General Public License along with this
//  program.  If not, see <http://www.gnu.org/licenses/>.
//------------------------------------------------------------------------------------------------//
// </editor-fold>
package omr.image;

/**
 * Class {@code ImageFormatException} describes an exception raised when a non-handled
 * format is detected in an image file.
 *
 * @author Hervé Bitteur
 */
public class ImageFormatException
        extends Exception
{
    //~ Constructors -------------------------------------------------------------------------------

    /**
     * Construct an {@code ImageFormatException} with provided detail message.
     */
    public ImageFormatException (String message)
    {
        super(message);
    }
}
