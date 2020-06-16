program voronoi_diagram(inddata,uddata);
   uses btree;                  {opbygning af intern rep.}
   const eps = 5e-11;           {maskin n›jagtighed}
   type conveks_type_pil = ^conveks_type;
        conveks_type = record
                          p:point;
                          front,back:conveks_type_pil;
                       end;
        color = (grey,black);
        i_tree_pil = ^i_tree;
        i_tree = record
                   case node:color of
                      grey:(lft,rgt:i_tree_pil;
                            up_lft,up_rgt:point;     {›vre og            }
                            down_lft,down_rgt:point; {nedre brosegmenter }
                            CH:conveks_type_pil);
                      black:(p:point);
                 end;
        DCEL_type_pil = ^DCEL_type;
        DCEL = record
                  a_b,a_e:real;
                  f_l,f_r:point;
                  pkt,ret:point;
                  p_e,p_b:DCEL_type_pil;
                  used:boolean;
               end;
        node_type = (vertices,edge);
        DCEL_type = record
                      case node:node_type of
                         edge:(D:DCEL);
                         vertices:(P:point);
                    end;
        vertices_pair = record
                           lft,rgt:point;
                        end;
        point_type = (concave,reflex,support);
        e_type = (uendlig,semi,lukket);
        cut_type = (yes,no,four_point,parallel);
        next_pil = ^next_edge;
        next_edge = record
                       D_pil:DCEL_type_pil;
                       next:next_pil;
                    end;
   var r:AVL_tree_pil;
       i:i_tree_pil;
       v:DCEL_type_pil;
       inddata,uddata:text;
       indfile,udfile:string[25];
       x_max,x_min,y_max,y_min:real;

   function interval_tree(root:AVL_tree_pil):i_tree_pil;
      var node:i_tree_pil;
          min,max,average:point;
      procedure right(root:AVL_tree_pil; var max:point);
      begin
         if root^.rgt = nil
         then max:=root^.p
         else right(root^.rgt,max)
      end;
      procedure left(root:AVL_tree_pil; var min:point);
      begin
         if root^.lft = nil
         then min:=root^.p
         else left(root^.lft,min)
      end;
   begin
      new(node);
      if (root^.lft = nil) and (root^.rgt = nil)
      then begin
         node^.node:=black;
         node^.p:=root^.p;
         dispose(root)
      end
      else begin
         node^.node:=grey;
         node^.CH:=nil;
         right(root,max); left(root,min);
         average.x:=(min.x+max.x)/2; average.y:=(min.y+max.y)/2;
         if (average.x > root^.p.x) or
            ((average.x = root^.p.x) and (average.y > root^.p.y))
         then begin
            node^.rgt:=interval_tree(root^.rgt);
            root^.rgt:=nil;
            node^.lft:=interval_tree(root)
         end
         else begin
            node^.lft:=interval_tree(root^.lft);
            root^.lft:=nil;
            node^.rgt:=interval_tree(root)
         end;
      end;
      interval_tree:=node
   end;
   function same(pkt1,pkt2:point):boolean;
   begin
      same:=(pkt1.x = pkt2.x) and (pkt1.y = pkt2.y)
   end;
   procedure Conveks_Hull(root:i_tree_pil);
      function To_pkt_CH(lft,rgt:point):conveks_type_pil;
         var temp,temp1:conveks_type_pil;
      begin
         new(temp);
         temp^.p:=lft;
         new(temp1);
         temp1^.p:=rgt;
         temp1^.front:=temp; temp1^.back:=temp;
         temp^.front:=temp1; temp^.back:=temp1;
         To_pkt_CH:=temp;
      end;
      procedure bridge(lft,rgt:i_tree_pil; var node:i_tree);
         var up_lft,down_lft,up_rgt,down_rgt:conveks_type_pil;
             temp:conveks_type_pil;
             done_up_lft,done_down_lft,done_up_rgt,done_down_rgt:boolean;
         function Areal(a,b,c:point):integer;
            var temp:real;
         begin
            temp:=(c.y-b.y)*(b.x-a.x)-(b.y-a.y)*(c.x-b.x);
            if (temp-eps) > 0
            then Areal:=1
            else if (temp+eps) < 0
                 then Areal:=-1
                 else Areal:=0
         end;
         function ret(s_p,e_p,p:point):integer;
            var temp:real;
         begin
            temp:=(e_p.y-s_p.y)*(p.y-s_p.y)+(e_p.x-s_p.x)*(p.x-s_p.x);
            if (temp-eps) > 0
            then ret:=1
            else if (temp+eps) < 0
                 then ret:=-1
                 else ret:=0
         end;
         procedure fjern_fra_CH(next,stop:conveks_type_pil);
            var t:conveks_type_pil;
         begin
            t:=next;
            while next <> stop do begin
               next:=next^.front;
               dispose(t);
               t:=next
            end
         end;
         function test(pkt:point; e_p:conveks_type_pil):point_type;
            var front,back:integer;
         begin
            back:=Areal(pkt,e_p^.p,e_p^.back^.p);
            front:=Areal(pkt,e_p^.p,e_p^.front^.p);
            if back*front >= 0
            then test:=support
            else if back >= 0
                 then test:=reflex
                 else test:=concave;
         end;
         procedure find_up(pkt:point; var up_p:conveks_type_pil;
                           var stop:boolean);
            var done:boolean;
                start:conveks_type_pil;
         begin
            done:=false; start:=up_p;
            repeat
               case test(pkt,up_p) of
                  concave:up_p:=up_p^.back;
                   reflex:up_p:=up_p^.front;
                  support:case Areal(pkt,up_p^.p,up_p^.back^.p) of
                             0:if (up_p^.front = up_p^.back) and
                                  (ret(up_p^.p,up_p^.back^.p,pkt) < 0)
                               then done:=true
                               else up_p:=up_p^.back;
                             1:done:=true;
                            -1:up_p:=up_p^.back;
                          end;
               end;
            until done;
            stop:=(up_p = start);
         end;
         procedure find_down(pkt:point; var down_p:conveks_type_pil;
                             var stop:boolean);
            var done:boolean;
                start:conveks_type_pil;
         begin
            done:=false; start:=down_p;
            repeat
               case test(pkt,down_p) of
                  concave:down_p:=down_p^.front;
                   reflex:down_p:=down_p^.back;
                  support:case Areal(pkt,down_p^.p,down_p^.front^.p) of
                             0:if (down_p^.front = down_p^.back) and
                                  (ret(down_p^.p,down_p^.front^.p,pkt) < 0)
                               then done:=true
                               else down_p:=down_p^.front;
                             1:down_p:=down_p^.front;
                            -1:done:=true;
                          end;
               end;
            until done;
            stop:=(down_p = start);
         end;
      begin
         if lft^.node = black
         then begin
            node.up_lft:=lft^.p; node.down_lft:=lft^.p;
            up_rgt:=rgt^.CH; down_rgt:=rgt^.CH;
            find_up(lft^.p,down_rgt,done_down_rgt);
            find_down(lft^.p,up_rgt,done_up_rgt);
            node.up_rgt:=up_rgt^.p; node.down_rgt:=down_rgt^.p;
            new(temp);
            temp^.p:=lft^.p;
            fjern_fra_CH(up_rgt^.front,down_rgt);
            up_rgt^.front:=temp; temp^.back:=up_rgt;
            temp^.front:=down_rgt; down_rgt^.back:=temp;
            node.CH:=temp;
         end
         else if rgt^.node = black
              then begin
                 node.up_rgt:=rgt^.p; node.down_rgt:=rgt^.p;
                 up_lft:=lft^.CH; down_lft:=lft^.CH;
                 find_up(rgt^.p,up_lft,done_up_lft);
                 find_down(rgt^.p,down_lft,done_down_lft);
                 node.up_lft:=up_lft^.p; node.down_lft:=down_lft^.p;
                 new(temp);
                 temp^.p:=rgt^.p;
                 fjern_fra_CH(down_lft^.front,up_lft);
                 down_lft^.front:=temp; temp^.back:=down_lft;
                 temp^.front:=up_lft; up_lft^.back:=temp;
                 node.CH:=temp;
              end
              else begin
                 up_lft:=lft^.CH; up_rgt:=rgt^.CH;
                 down_lft:=up_lft; down_rgt:=up_rgt;
                 repeat
                    find_up(up_rgt^.p,up_lft,done_up_lft);
                    find_down(down_rgt^.p,down_lft,done_down_lft);
                    find_up(down_lft^.p,down_rgt,done_down_rgt);
                    find_down(up_lft^.p,up_rgt,done_up_rgt);
                 until (done_up_lft and done_down_lft) and
                       (done_up_rgt and done_down_rgt);
                 node.up_lft:=up_lft^.p; node.down_lft:=down_lft^.p;
                 node.up_rgt:=up_rgt^.p; node.down_rgt:=down_rgt^.p;
                 fjern_fra_CH(down_lft^.front,up_lft);
                 fjern_fra_CH(up_rgt^.front,down_rgt);
                 up_rgt^.front:=up_lft; up_lft^.back:=up_rgt;
                 down_lft^.front:=down_rgt; down_rgt^.back:=down_lft;
                 node.CH:=up_lft; {den er tilf‘ldigt valgt}
              end;
         if same(node.up_lft,node.down_lft) and
            same(node.up_rgt,node.down_rgt)
         then begin
            writeln(uddata,'ERROR: 3 eller 4 punkter p† linie');
            halt
         end;
      end;
   begin
      with root^ do begin
         if node = grey
         then if (lft^.node = black) and (rgt^.node = black)
              then CH:=To_pkt_CH(lft^.p,rgt^.p)
              else begin
                 Conveks_Hull(lft);
                 Conveks_Hull(rgt);
                 bridge(lft,rgt,root^);
              end;
      end;
   end;

   function vor(root:i_tree_pil):DCEL_type_pil;
      function opret_point(pkt:point):DCEL_type_pil;
         var p:DCEL_type_pil;
      begin
         new(p);
         p^.node:=vertices;
         p^.P:=pkt;
         opret_point:=p
      end;
      function bisektor(lft_p,rgt_p:point):DCEL_type_pil;
         var pair:DCEL_type_pil;
      begin
         new(pair);
         pair^.node:=edge;
         with pair^.D do begin
            used:=false;
            a_b:=0; a_e:=0;
            f_l:=lft_p; f_r:=rgt_p;
            pkt.x:=(lft_p.x+rgt_p.x)/2;
            pkt.y:=(lft_p.y+rgt_p.y)/2;
            ret.x:=lft_p.y-rgt_p.y;
            ret.y:=rgt_p.x-lft_p.x;
            p_b:=nil; p_e:=nil;
         end;
         bisektor:=pair;
      end;
      function merge(node:i_tree; lft,rgt:DCEL_type_pil):DCEL_type_pil;
         var upper,lower:vertices_pair;
             lft_p,rgt_p,l_pkt,r_pkt:point;
             current,next,rgt_next,lft_next:DCEL_type_pil;
             l_next_edge,r_next_edge:next_pil;
             l_next_q,r_next_q:boolean;
         procedure cut_point(l1,l2:DCEL; var p:point);
            var a1,a2,b1,b2,c1,c2,d:real;
         begin
            a1:=l1.ret.x; a2:=l1.ret.y;
            b1:=-l2.ret.x; b2:=-l2.ret.y;
            d:=a1*b2-a2*b1;
            if ((d-eps) > 0) or ((d+eps) < 0)
            then begin   {forskellig fra nul, indenfor maskinn›j.}
               c1:=l2.pkt.x-l1.pkt.x; c2:=l2.pkt.y-l1.pkt.y;
               p.x:=(c1*b2-c2*b1)/d;
               p.y:=(a1*c2-a2*c1)/d;
            end
            else begin   {lig nul, indenfor maskinn›j.}
               p.x:=0; p.y:=0
            end
         end;

         function same_point(e:DCEL_type_pil; p:point):boolean;
         begin
            same_point:=same(e^.D.f_l,p) or same(e^.D.f_r,p)
         end;
         function edge_type(e:DCEL_type_pil):e_type;
         begin
            if (e^.D.p_b <> nil) and (e^.D.p_e <> nil)
            then edge_type:=lukket
            else if (e^.D.p_b = nil) and (e^.D.p_e = nil)
                 then edge_type:=uendlig
                 else edge_type:=semi
         end;
         function modur(q:DCEL_type_pil; pkt:point):DCEL_type_pil;
            var e:e_type;
         begin
            e:=edge_type(q);
            case e of
               uendlig:modur:=q;
                  semi:if q^.D.p_b <> nil
                       then modur:=q^.D.p_b
                       else modur:=q^.D.p_e;
                lukket:if same_point(q^.D.p_e,pkt)
                       then modur:=q^.D.p_e
                       else modur:=q^.D.p_b;
            end;
         end;
         function medur(p:DCEL_type_pil; pkt:point):DCEL_type_pil;
            var e:e_type;
         begin
            e:=edge_type(p);
            case e of
               uendlig:medur:=p;
                  semi:if p^.D.p_b <> nil
                       then medur:=modur(p^.D.p_b,pkt)
                       else medur:=modur(p^.D.p_e,pkt);
                lukket:if same_point(p^.D.p_b,pkt)
                       then medur:=modur(p^.D.p_e,pkt)
                       else medur:=modur(p^.D.p_b,pkt);
            end;
         end;

         function test_cut(next:DCEL_type_pil):cut_type;
            var l:point;
                n_cut,c_cut,is_4_point:boolean;
         begin
            is_4_point:=false;
            cut_point(next^.D,current^.D,l);
            if (l.x <> 0) or (l.y <> 0)
            then begin    {ikke parallele kanter}
               case edge_type(next) of
                  uendlig:n_cut:=true;
                     semi:with next^.D do
                             if p_b <> nil
                             then begin
                                is_4_point:=l.x = a_b;
                                n_cut:=l.x > a_b
                             end
                             else begin
                                is_4_point:=l.x = a_e;
                                n_cut:=l.x < a_e
                             end;
                   lukket:with next^.D do begin
                             is_4_point:=(l.x = a_b) or (l.x = a_e);
                             n_cut:=(l.x > a_b) and (l.x < a_e)
                          end;
               end;
               case edge_type(current) of
                  uendlig:c_cut:=true;
                     semi:with current^.D do begin
                             is_4_point:=is_4_point and (l.y < a_e);
                             c_cut:=l.y < a_e
                          end;
               end;
               if is_4_point
               then begin
                  writeln(uddata,'Warning: 4-pkt ko-circul‘re');
                  test_cut:=four_point;
               end
               else if n_cut and c_cut
                    then test_cut:=yes
                    else test_cut:=no
            end
            else test_cut:=parallel
         end;
         function line_cut(edge1,edge2,edge3:DCEL_type_pil;
                           tilfaelde:integer):DCEL_type_pil;
            var p,q:point;
                t_edge:next_pil;
            procedure edge_order(var l_r_mgd:next_pil; n_edge:DCEL_type_pil);
               procedure edge_q(var next:next_pil);
               begin
                  new(next);
                  next^.D_pil:=n_edge;
                  next^.next:=nil;
               end;
            begin
               if l_r_mgd = nil
               then begin
                  edge_q(l_r_mgd);
                  t_edge:=l_r_mgd
               end
               else begin
                  edge_q(t_edge^.next);
                  t_edge:=t_edge^.next
               end;
            end;
            procedure type_lft(next:DCEL_type_pil);
               var temp:DCEL_type_pil;
            begin
               if lft_next^.D.p_b = next
               then lft_next^.D.p_b:=nil
               else lft_next^.D.p_e:=nil;
               while edge_type(next) = lukket do begin
                  temp:=next;
                  if next^.D.ret.x >= 0
                  then begin
                     next:=next^.D.p_e^.D.p_e;
                     if edge_type(temp^.D.p_e) = semi
                     then edge_order(l_next_edge,temp^.D.p_e);
                     temp^.D.p_e^.D.p_e:=nil
                  end
                  else begin
                     next:=medur(next,next^.D.f_r);
                     if edge_type(temp^.D.p_b) = semi
                     then edge_order(l_next_edge,temp^.D.p_b);
                     if temp^.D.p_b^.D.p_e = next
                     then temp^.D.p_b^.D.p_e:=nil
                     else temp^.D.p_b^.D.p_b:=nil
                  end;
                  dispose(temp);
               end;
               dispose(next);
            end;
            procedure type_rgt(next:DCEL_type_pil);
               var t,temp:DCEL_type_pil;
            begin
               if rgt_next^.D.p_b = edge2
               then rgt_next^.D.p_b:=nil
               else rgt_next^.D.p_e:=nil;
               while edge_type(next) = lukket do begin
                  temp:=next;
                  if next^.D.ret.x >= 0
                  then begin
                     next:=next^.D.p_b;
                     t:=medur(temp,temp^.D.f_r);
                     if edge_type(t) = semi
                     then edge_order(r_next_edge,t);
                     t^.D.p_e:=nil
                  end
                  else begin
                     next:=medur(next,next^.D.f_l);
                     if edge_type(next) = semi
                     then edge_order(r_next_edge,next);
                     next^.D.p_e:=nil
                  end;
                  dispose(temp);
               end;
               dispose(next);
            end;
         begin
            cut_point(edge3^.D,edge2^.D,q);
            cut_point(edge2^.D,edge1^.D,p);
            edge3^.D.a_b:=q.x;
            edge1^.D.a_e:=p.y;
            case tilfaelde of
               1:begin
                    if edge2^.D.p_b <> nil
                    then begin
                       lft_next:=edge2^.D.p_b;
                       type_lft(modur(lft_next,edge2^.D.f_r));
                    end;
                    edge2^.D.a_b:=q.y;
                    edge3^.D.p_b:=edge2;
                    edge2^.D.p_b:=edge1;
                    edge1^.D.p_e:=edge3;
                 end;
               2:begin
                    if edge2^.D.p_e <> nil
                    then begin
                       lft_next:=edge2^.D.p_e;
                       type_lft(edge2^.D.p_e^.D.p_e);
                    end;
                    edge2^.D.a_e:=q.y;
                    edge3^.D.p_b:=edge2;
                    edge2^.D.p_e:=edge1;
                    edge1^.D.p_e:=edge3;
                 end;
               3:begin
                    if edge2^.D.p_b <> nil
                    then begin
                       rgt_next:=modur(edge2^.D.p_b,edge2^.D.f_r);
                       type_rgt(edge2^.D.p_b);
                    end;
                    edge2^.D.a_b:=q.y;
                    edge3^.D.p_b:=edge1;
                    edge1^.D.p_e:=edge2;
                    edge2^.D.p_b:=edge3;
                 end;
               4:begin
                    if edge2^.D.p_e <> nil
                    then begin
                       rgt_next:=edge2^.D.p_e^.D.p_b;
                       type_rgt(edge2^.D.p_e);
                    end;
                    edge2^.D.a_e:=q.y;
                    edge3^.D.p_b:=edge1;
                    edge1^.D.p_e:=edge2;
                    edge2^.D.p_e:=edge3;
                 end;
            end;
            l_next_q:=((edge_type(lft_next) = uendlig) or (lft = edge2))
                      and (l_next_edge <> nil);
            r_next_q:=((edge_type(rgt_next) = uendlig) or (rgt = edge2))
                      and (r_next_edge <> nil);
            if lft_next <> nil
            then lft:=lft_next;
            if rgt_next <> nil
            then rgt:=rgt_next;
            line_cut:=edge1
         end;
         function find:DCEL_type_pil;
            var l,r:point;
                lft_cut_point,rgt_cut_point:real;
                lft_cut,rgt_cut:cut_type;
            procedure lft_mgd;
               var t,temp:next_pil;
            begin
               t:=l_next_edge;
               while (test_cut(lft_next) = no) do
                  if l_next_q and (t <> nil)
                  then begin
                     temp:=t;
                     lft_next:=t^.D_pil;
                     t:=t^.next;
                     l_next_edge:=t;
                     dispose(temp);
                  end
                  else lft_next:=modur(lft_next,lft_p);
               if lft_next = current
               then lft_next:=lft
               else lft:=lft_next;
            end;
            procedure rgt_mgd;
               var t,temp:next_pil;
            begin
               t:=r_next_edge;
               while (test_cut(rgt_next) = no) do
                  if r_next_q and (t <> nil)
                  then begin
                     temp:=t;
                     rgt_next:=t^.D_pil;
                     t:=t^.next;
                     r_next_edge:=t;
                     dispose(temp);
                  end
                  else rgt_next:=medur(rgt_next,rgt_p);
               if rgt_next = current
               then rgt_next:=rgt
               else rgt:=rgt_next;
            end;
            procedure move_lft;
               var next_p:point;
                   stop:boolean;
                   start,next:DCEL_type_pil;
            begin
               if same(lft_next^.D.f_l,lft_p)
               then next_p:=lft_next^.D.f_r
               else next_p:=lft_next^.D.f_l;
               next:=medur(lft_next,next_p);
               start:=lft_next;
               stop:=(next = nil) or (edge_type(lft) = lukket);
               while not stop do begin
                  lft_next:=next;
                  next:=medur(next,next_p);
                  stop:=(edge_type(lft_next) = semi) or (next = start);
               end
            end;
            procedure move_rgt;
               var next_p:point;
                   stop:boolean;
                   start,next:DCEL_type_pil;
            begin
               if same(rgt_next^.D.f_l,rgt_p)
               then next_p:=rgt_next^.D.f_r
               else next_p:=rgt_next^.D.f_l;
               next:=modur(rgt_next,next_p);
               start:=rgt_next;
               stop:=(next = nil) or (edge_type(rgt) = lukket);
               while not stop do begin
                  rgt_next:=next;
                  next:=modur(next,next_p);
                  stop:=(edge_type(rgt_next) = semi) or (next = start);
               end
            end;
         begin
            if (lft_next <> nil) and (rgt_next <> nil)
            then begin {find kant hvor sk‘ring med y er st›rst}
               lft_mgd; lft_cut:=test_cut(lft_next);
               rgt_mgd; rgt_cut:=test_cut(rgt_next);
               cut_point(lft_next^.D,current^.D,l);
               cut_point(rgt_next^.D,current^.D,r);
               lft_cut_point:=l.x*lft_next^.D.ret.y+lft_next^.D.pkt.y;
               rgt_cut_point:=r.x*rgt_next^.D.ret.y+rgt_next^.D.pkt.y;
               if ((lft_cut = four_point) and (rgt_cut = yes) and
                   (lft_cut_point >= rgt_cut_point)) or
                  ((rgt_cut = four_point) and (lft_cut = yes) and
                   (rgt_cut_point >= lft_cut_point))
               then begin
                  writeln(uddata,'ERROR: 4-point cocircular');
                  halt
               end;
               if (lft_cut = yes) and (rgt_cut = yes)
               then begin
                  if lft_cut_point > rgt_cut_point
                  then begin
                     find:=lft_next;
                     move_lft
                  end
                  else if rgt_cut_point > lft_cut_point
                       then begin
                          find:=rgt_next;
                          move_rgt
                       end
                       else begin
                          write(uddata,'ERROR: lft and rgt meet');
                          writeln(uddata,' with current');
                          halt
                       end
               end
               else if lft_cut = yes
                    then begin
                       find:=lft_next;
                       move_lft
                    end
                    else if rgt_cut = yes
                         then begin
                            find:=rgt_next;
                            move_rgt
                         end
                         else begin
                            writeln(uddata,'ERROR: no next edge');
                            halt
                         end
            end
            else if lft_next <> nil
                 then begin
                    lft_mgd;
                    find:=lft_next;
                    move_lft
                 end
                 else begin
                    rgt_mgd;
                    find:=rgt_next;
                    move_rgt
                 end
         end;
      begin
         lft_p:=node.up_lft; rgt_p:=node.up_rgt;
         current:=bisektor(lft_p,rgt_p);
         merge:=current;
         l_next_edge:=nil; r_next_edge:=nil;
         l_next_q:=false; r_next_q:=false;
         if not (lft^.node = vertices)
         then begin
            lft_next:=lft;
            if edge_type(lft_next) <> uendlig
            then begin
               while not same_point(lft_next,lft_p) do begin
                  if edge_type(lft_next) = semi
                  then if lft_next^.D.p_b <> nil
                       then l_pkt:=lft_next^.D.f_l
                       else l_pkt:=lft_next^.D.f_r;
                  lft_next:=modur(lft_next,l_pkt);
               end;
               while not same_point(modur(lft_next,lft_p),lft_p) or
                     (edge_type(lft_next) = lukket) do
                  lft_next:=medur(lft_next,lft_p);
               lft:=lft_next;
            end
         end
         else lft_next:=nil;
         if not (rgt^.node = vertices)
         then begin
            rgt_next:=rgt;
            if edge_type(rgt_next) <> uendlig
            then begin
               while not same_point(rgt_next,rgt_p) do begin
                  if edge_type(rgt_next) = semi
                  then if rgt_next^.D.p_b <> nil
                       then r_pkt:=rgt_next^.D.f_r
                       else r_pkt:=rgt_next^.D.f_l;
                  rgt_next:=medur(rgt_next,r_pkt);
               end;
               while same_point(modur(rgt_next,rgt_p),rgt_p) or
                     (edge_type(rgt_next) = lukket) do
                  rgt_next:=modur(rgt_next,rgt_p);
               rgt:=rgt_next;
            end
         end
         else rgt_next:=nil;
         repeat
            next:=find;
            if same_point(next,current^.D.f_l)
            then if same(next^.D.f_l,current^.D.f_l)
                 then begin
                    lft_p:=next^.D.f_r;
                    current:=line_cut(bisektor(lft_p,rgt_p),next,current,2);
                 end
                 else begin
                    lft_p:=next^.D.f_l;
                    current:=line_cut(bisektor(lft_p,rgt_p),next,current,1);
                 end
            else if same(next^.D.f_r,current^.D.f_r)
                 then begin
                    rgt_p:=next^.D.f_l;
                    current:=line_cut(bisektor(lft_p,rgt_p),next,current,4);
                 end
                 else begin
                    rgt_p:=next^.D.f_r;
                    current:=line_cut(bisektor(lft_p,rgt_p),next,current,3)
                 end;
         until same(lft_p,node.down_lft) and same(rgt_p,node.down_rgt);
         if lft^.node = vertices
         then dispose(lft)
         else if rgt^.node = vertices
              then dispose(rgt)
      end;
   begin
      with root^ do begin
         if node = black
         then vor:=opret_point(p)
         else if (lft^.node = black) and (rgt^.node = black)
              then begin
                 vor:=bisektor(lft^.p,rgt^.p);
                 dispose(lft);
                 dispose(rgt)
              end
              else vor:=merge(root^,vor(lft),vor(rgt));
         dispose(root)
      end
   end;
   procedure write_voronoi(root:DCEL_type_pil);
   {funktion: udskriv VD's kundepunkters koordinater, samt de adskil- }
   {          lende punkter                                           }
   {kaldsted: hovedprogrammet, write_voronoi (rekursiv)               }
   {parametre: en kant der repr‘senterer VD, det er den sidste merge- }
   {           skridts f›rste kant                                    }
   {sideeffekt: ingen                                                 }
      var p,q:point;
          t,afs:real;
      procedure koordinat(param:real; pkt,ret:point; var koor:point);
      {funktion: beregner knudepunktets koordinat  }
      {kaldsted: write_voronoi                     }
      {parametre: param; parameterv‘rdien af knude-}
      {           punktets koordinat. pkt, ret;    }
      {           kantens parameterfremstilling    }
      {           koor; koordinaten                }
      {sideeffekt: ingen                           }
      begin
         koor.x:=param*ret.x+pkt.x;
         koor.y:=param*ret.y+pkt.y;
      end;
   begin
      if root <> nil
      then with root^.D do
         if not used
         then begin
            used:=true;
            if (p_b <> nil) and (p_e <> nil)
            then begin
               koordinat(a_b,pkt,ret,p);
               koordinat(a_e,pkt,ret,q);
               writeln(uddata,p.x:10:2,' ',q.x:10:2,' ',
                              f_l.x:10:2,' ',f_r.x:10:2);
               writeln(uddata,p.y:10:2,' ',q.y:10:2,' ',
                              f_l.y:10:2,' ',f_r.y:10:2)
            end
            else begin
               if p_b <> nil
               then koordinat(a_b,pkt,ret,p)
               else koordinat(a_e,pkt,ret,p);
               afs:=sqr(f_l.x-p.x)+sqr(f_l.y-p.y);
               t:=sqrt(afs/(sqr(ret.x)+sqr(ret.y)));
               if p_e <> nil
               then t:=-t;
               koordinat(t,pkt,ret,q);
               if p_b <> nil
               then begin
                  if q.x > x_max
                  then x_max:=q.x;
                  if q.x < x_min
                  then x_min:=q.x;
                  if q.y > y_max
                  then y_max:=q.y;
                  writeln(uddata,p.x:10:2,' ',q.x:10:2,' ',
                                 f_l.x:10:2,' ',f_r.x:10:2);
                  writeln(uddata,p.y:10:2,' ',q.y:10:2,' ',
                                 f_l.y:10:2,' ',f_r.y:10:2)
               end
               else begin
                  if q.x > x_max
                  then x_max:=q.x;
                  if q.x < x_min
                  then x_min:=q.x;
                  if q.y < y_min
                  then y_min:=q.y;
                  writeln(uddata,q.x:10:2,' ',p.x:10:2,' ',
                                 f_l.x:10:2,' ',f_r.x:10:2);
                  writeln(uddata,q.y:10:2,' ',p.y:10:2,' ',
                                 f_l.y:10:2,' ',f_r.y:10:2)
               end
            end;
            write_voronoi(p_e);
            write_voronoi(p_b)
         end
   end;

begin
   write('Indtast inddata file: '); readln(indfile);
   write('Indtast uddata file: '); readln(udfile);
   assign(inddata,indfile); reset(inddata);
   assign(uddata,udfile); rewrite(uddata);
   r:=nil;
   while not eof(inddata) do begin
      readln(inddata,pkt.x,pkt.y);
      opbyg_tree(r);
   end;
   i:=interval_tree(r);
   Conveks_Hull(i);
   v:=vor(i);
   x_max:=v^.D.f_l.x; x_min:=v^.D.f_l.x;
   y_max:=v^.D.f_l.y; y_min:=v^.D.f_l.y;
   write_voronoi(v);
   writeln(uddata,'e');
   writeln(uddata,x_min:10:2,' ',y_min:10:2,' ',
                  (x_max-x_min):10:2,' ',(y_max-y_min):10:2);
   close(uddata)
end.