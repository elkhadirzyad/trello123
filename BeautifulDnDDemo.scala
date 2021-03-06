package react.beautifuldnd.demo

import cats._
import cats.syntax.all._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom.document
import react.beautifuldnd._
import com.kmse.hicp.client.components.basic._

// Adaptation of https://codesandbox.io/s/k260nyxq9v
object BeautifulDnDDemo {
  private val Grid = 8

  protected case class Item(id: String, content: String)

  private def getItems(n: Int): List[Item] =
    (0 until n).map(i => Item(s"item-$i", s"item $i")).toList

  final case class Props()
  final case class State(list: List[Item] = getItems(10), list1: List[Item] = getItems(10), list2: List[Item] = getItems(10))

  implicit object CallbackMonoid extends Monoid[Callback] {
    def empty: Callback = Callback.empty
    def combine(x: Callback, y: Callback): Callback = x *> y
  }

  class Backend($ : BackendScope[Props, State]) {
    def reorder[A](source: Int, destination: Int)(list: List[A]): List[A] = {
      val removed      = list(source)
      val intermediate = list.take(source) ++ list.drop(source + 1)
      (intermediate.take(destination) :+ removed) ++ intermediate.drop(destination)
    }

    val onDragEnd: (DropResult, ResponderProvided) => Callback =
      (result, _) =>
        result.destination.toOption.foldMap[Callback] { destination =>
          $.modState(s => State(reorder(result.source.index, destination.index)(s.list)))
        }

    def getListStyle(isDraggingOver: Boolean): TagMod =
      TagMod(
        (^.background := "lightgrey").unless(isDraggingOver),
        (^.background := "lightblue").when(isDraggingOver),
        ^.padding := s"${Grid}px",
        ^.width := "250px"
      )

    def getItemStyle(isDragging: Boolean, draggableStyle: TagMod): TagMod =
      TagMod(
        // some basic styles to make the items look a bit nicer
        // ^.userSelect := "none",
        ^.padding := s"${Grid * 2}px",
        ^.margin := s"0 0 ${Grid}px 0",
        // change background colour if dragging
        (^.background := "grey").unless(isDragging),
        (^.background := "lightgreen").when(isDragging),
        // styles we need to apply on draggables
        draggableStyle
      )

    def render(s: State): VdomElement =
      <.div(
        <.div(^.display := "flex")(
          DragDropContext(onDragEnd = onDragEnd)(
            Droppable("droppableList") { case (provided, snapshot) =>
              <.div(
                provided.innerRef,
                provided.droppableProps,
                getListStyle(snapshot.isDraggingOver)
              )(
                <.b("Good to go:"),
                s.list.zipWithIndex.toTagMod { case (item, index) =>
                  Draggable(item.id, index) { case (provided, snapshot, _) =>
                    <.div(
                      HicpCardWithCheckboxAvatarHeader(title = "my card header"+item.content+50, subheader = "mysubheader", className = "card")("A domain-specific language (DSL) is a computer language"),
                      provided.innerRef,
                      provided.draggableProps,
                      provided.dragHandleProps,
                      getItemStyle(snapshot.isDragging, provided.draggableStyle)
                    )(
                    // item.content
                     // HicpFlatButton(label="text")()
                    )
                  }
                },

                provided.placeholder
              )
            },

           Droppable("droppableList1") { case (provided, snapshot) =>
              <.div(
                provided.innerRef,
                provided.droppableProps,
                getListStyle(snapshot.isDraggingOver)
              )(
               // <.b("Good to go:"),
               /* s.list1.zipWithIndex.toTagMod { case (item, index) =>
                  Draggable(item.id, index) { case (provided, snapshot, _) =>
                    <.div(
                      HicpCardWithCheckboxAvatarHeader(title = "my card header"+item.content+90, subheader = "mysubheader", className = "card")("A domain-specific language (DSL) is a computer language"),
                     // (" A domain-specific language (DSL) is ...."))),
                      provided.innerRef,
                      provided.draggableProps,
                      provided.dragHandleProps,
                      getItemStyle(snapshot.isDragging, provided.draggableStyle)
                    )(
                      // item.content
                      // HicpFlatButton(label="text")()
                    )
                  }
                },*/

              //  provided.placeholder
              )
            } /*,

            Droppable("droppableList2") { case (provided, snapshot) =>
              <.div(
                provided.innerRef,
                provided.droppableProps,
                getListStyle(snapshot.isDraggingOver)
              )(
                <.b("Good to go:"),
                s.list2.zipWithIndex.toTagMod { case (item, index) =>
                  Draggable(item.id, index) { case (provided, snapshot, _) =>
                    <.div(
                      HicpCardWithCheckboxAvatarHeader(title = "my card header"+item.content+70, subheader = "mysubheader", className = "card")("A domain-specific language (DSL) is a computer language"),
                      provided.innerRef,
                      provided.draggableProps,
                      provided.dragHandleProps,
                      getItemStyle(snapshot.isDragging, provided.draggableStyle)
                    )(
                      // item.content
                      // HicpFlatButton(label="text")()
                    )
                  }
                },

                provided.placeholder
              )
            } */
          )
        )
      )
  }

  val component = ScalaComponent
    .builder[Props]("BeautifulDnDDemo")
    .initialState(State())
    .backend(new Backend(_))
    .renderBackend
    .build

  def apply(p: Props) = component(p)
}

object MainDemo {
  val component = ScalaComponent
    .builder[Unit]("Demo")
    .stateless
    .render_P { _ =>
      <.div(
        BeautifulDnDDemo(BeautifulDnDDemo.Props()).vdomElement
       // BeautifulDnDDemo(BeautifulDnDDemo.Props()).vdomElement
      )
    }
    .build

  def main(args: Array[String]): Unit = {
    component().renderIntoDOM(document.getElementById("root"))
    ()
  }
}
